package com.example.demo.service;

import com.example.demo.dto.AgendamentoRequest;
import com.example.demo.dto.ValidacaoRequest;
import com.example.demo.model.Agendamento;
import com.example.demo.model.Cadastro;
import com.example.demo.model.Veiculo;
import com.example.demo.repository.AgendamentoRepository;
import com.example.demo.repository.CadastroRepository;
import com.example.demo.repository.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE: regras dos agendamentos.
 *  - o cliente agenda só para um carro DELE, numa data futura,
 *    de segunda a sábado, das 8h às 17h, num horário livre
 *  - o mecânico (admin) confirma ou recusa um agendamento PENDENTE
 */
@Service
public class AgendamentoService {

    private static final LocalTime ABRE = LocalTime.of(8, 0);
    private static final LocalTime ULTIMO_HORARIO = LocalTime.of(17, 0);

    private final AgendamentoRepository agendamentoRepository;
    private final CadastroRepository cadastroRepository;
    private final VeiculoRepository veiculoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository, CadastroRepository cadastroRepository,
                              VeiculoRepository veiculoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.cadastroRepository = cadastroRepository;
        this.veiculoRepository = veiculoRepository;
    }

    // ===== MECÂNICO (admin) =====

    public List<Agendamento> listarTodos() {
        return agendamentoRepository.findAll();
    }

    public Agendamento validar(Long id, ValidacaoRequest dados) {
        Agendamento a = buscar(id);
        if (!Agendamento.PENDENTE.equals(a.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este agendamento já foi validado");
        }
        // Não deixa confirmar dois carros no mesmo horário
        if (Agendamento.CONFIRMADO.equals(dados.status())
                && agendamentoRepository.horarioOcupadoConfirmado(agendamentoRepository.dataHoraDe(id), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe outro agendamento confirmado nesse horário");
        }
        String resposta = dados.resposta() == null || dados.resposta().isBlank() ? null : dados.resposta().trim();
        agendamentoRepository.atualizarStatus(id, dados.status(), resposta);
        return buscar(id);
    }

    // ===== CLIENTE =====

    public List<Agendamento> meus(String emailLogado) {
        Optional<Cadastro> cadastro = cadastroRepository.findAtivoByEmail(emailLogado);
        return cadastro.map(c -> agendamentoRepository.findByClienteId(c.getId())).orElse(List.of());
    }

    public Agendamento solicitar(String emailLogado, AgendamentoRequest dados) {
        Cadastro cadastro = cadastroRepository.findAtivoByEmail(emailLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Seu login não tem um cadastro de cliente ativo"));
        Veiculo veiculo = veiculoRepository.findById(dados.veiculoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));
        if (!veiculo.getClienteId().equals(cadastro.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este veículo não é seu");
        }

        LocalDateTime quando = converter(dados.data(), dados.hora());
        if (!quando.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escolha uma data e horário no futuro");
        }
        if (quando.getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A oficina não abre aos domingos");
        }
        LocalTime hora = quando.toLocalTime();
        if (hora.isBefore(ABRE) || hora.isAfter(ULTIMO_HORARIO)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário de atendimento: das 8h às 17h");
        }

        Timestamp ts = Timestamp.valueOf(quando);
        if (agendamentoRepository.horarioOcupado(ts, null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esse horário já está reservado. Escolha outro.");
        }

        String motivo = dados.motivo() == null || dados.motivo().isBlank() ? null : dados.motivo().trim();
        Long id = agendamentoRepository.insert(veiculo.getId(), ts, motivo);
        return buscar(id);
    }

    // ===== auxiliares =====

    private Agendamento buscar(Long id) {
        return agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento #" + id + " não encontrado"));
    }

    // "2026-11-20" + "09:00" -> LocalDateTime
    private LocalDateTime converter(String data, String hora) {
        try {
            return LocalDateTime.of(LocalDate.parse(data), LocalTime.parse(hora));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data ou horário inválido");
        }
    }
}
