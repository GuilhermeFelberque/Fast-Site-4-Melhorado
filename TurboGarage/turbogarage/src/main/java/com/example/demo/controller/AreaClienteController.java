package com.example.demo.controller;

import com.example.demo.dto.AgendamentoRequest;
import com.example.demo.dto.MeuVeiculo;
import com.example.demo.dto.PreAtendimentoRequest;
import com.example.demo.dto.RelatorioOS;
import com.example.demo.model.Agendamento;
import com.example.demo.model.OrdemServico;
import com.example.demo.service.AgendamentoService;
import com.example.demo.service.AreaClienteService;
import com.example.demo.service.RelatorioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER: área do cliente (qualquer pessoa LOGADA).
 *   GET  /api/cliente/veiculos         -> meus veículos + ordens de cada um
 *   POST /api/cliente/pre-atendimento  -> pedir pré-atendimento (cria ordem SOLICITADA)
 *   GET  /api/cliente/agendamentos     -> meus agendamentos
 *   POST /api/cliente/agendamentos     -> pedir agendamento (fica PENDENTE até o mecânico validar)
 *   GET  /api/cliente/ordens/{id}/relatorio -> relatório de um serviço CONCLUÍDO do meu carro
 * Tudo é filtrado pelo email da sessão: ninguém vê ou mexe no carro de outra pessoa.
 */
@RestController
@RequestMapping("/api/cliente")
public class AreaClienteController {

    private final AreaClienteService service;
    private final AgendamentoService agendamentoService;
    private final RelatorioService relatorioService;

    public AreaClienteController(AreaClienteService service, AgendamentoService agendamentoService,
                                 RelatorioService relatorioService) {
        this.service = service;
        this.agendamentoService = agendamentoService;
        this.relatorioService = relatorioService;
    }

    @GetMapping("/ordens/{id}/relatorio")
    public RelatorioOS relatorio(@PathVariable Long id, HttpSession session) {
        return relatorioService.gerarParaCliente(emailLogado(session), id);
    }

    @GetMapping("/veiculos")
    public List<MeuVeiculo> meusVeiculos(HttpSession session) {
        return service.meusVeiculos(emailLogado(session));
    }

    @PostMapping("/pre-atendimento")
    public ResponseEntity<Map<String, Object>> preAtendimento(@Valid @RequestBody PreAtendimentoRequest dados,
                                                              HttpSession session) {
        OrdemServico o = service.solicitar(emailLogado(session), dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensagem", "Pré-atendimento enviado! A oficina vai analisar e definir o valor.",
                "registro", o));
    }

    @GetMapping("/agendamentos")
    public List<Agendamento> meusAgendamentos(HttpSession session) {
        return agendamentoService.meus(emailLogado(session));
    }

    @PostMapping("/agendamentos")
    public ResponseEntity<Map<String, Object>> agendar(@Valid @RequestBody AgendamentoRequest dados,
                                                       HttpSession session) {
        Agendamento a = agendamentoService.solicitar(emailLogado(session), dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensagem", "Agendamento enviado! Aguarde a confirmação do mecânico.",
                "registro", a));
    }

    // Sem login -> 401
    private String emailLogado(HttpSession session) {
        Object email = session.getAttribute(AuthController.SESSAO_EMAIL);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login para continuar");
        }
        return email.toString();
    }
}
