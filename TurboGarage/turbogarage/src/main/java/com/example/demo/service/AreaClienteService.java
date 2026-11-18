package com.example.demo.service;

import com.example.demo.dto.MeuVeiculo;
import com.example.demo.dto.PreAtendimentoRequest;
import com.example.demo.model.Cadastro;
import com.example.demo.model.OrdemServico;
import com.example.demo.model.Veiculo;
import com.example.demo.repository.CadastroRepository;
import com.example.demo.repository.OrdemServicoRepository;
import com.example.demo.repository.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * SERVICE: área do cliente (o diferencial).
 *  - o cliente vê SÓ os veículos dele e as ordens de cada um
 *  - o cliente pede um pré-atendimento (vira uma ordem com status SOLICITADA)
 */
@Service
public class AreaClienteService {

    private final CadastroRepository cadastroRepository;
    private final VeiculoRepository veiculoRepository;
    private final OrdemServicoRepository ordemRepository;

    public AreaClienteService(CadastroRepository cadastroRepository, VeiculoRepository veiculoRepository,
                              OrdemServicoRepository ordemRepository) {
        this.cadastroRepository = cadastroRepository;
        this.veiculoRepository = veiculoRepository;
        this.ordemRepository = ordemRepository;
    }

    // Veículos do cliente logado, cada um com as suas ordens de serviço
    public List<MeuVeiculo> meusVeiculos(String emailLogado) {
        Optional<Cadastro> cadastro = cadastroRepository.findAtivoByEmail(emailLogado);
        if (cadastro.isEmpty()) {
            return List.of(); // ex.: o admin não tem cadastro de cliente
        }
        Long clienteId = cadastro.get().getId();
        List<OrdemServico> ordens = ordemRepository.findByClienteId(clienteId);

        return veiculoRepository.findByClienteId(clienteId).stream()
                .map(v -> new MeuVeiculo(v.getId(), v.getPlaca(), v.getModelo(), v.getAno(), v.getObservacao(),
                        ordens.stream().filter(o -> o.getVeiculoId().equals(v.getId())).toList()))
                .toList();
    }

    // Pré-atendimento: o cliente descreve o problema; a oficina define valor e status depois
    public OrdemServico solicitar(String emailLogado, PreAtendimentoRequest dados) {
        Cadastro cadastro = cadastroRepository.findAtivoByEmail(emailLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Seu login não tem um cadastro de cliente ativo"));
        Veiculo veiculo = veiculoRepository.findById(dados.veiculoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));

        // Segurança: o cliente só pode pedir atendimento para um carro DELE
        if (!veiculo.getClienteId().equals(cadastro.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Este veículo não é seu");
        }

        OrdemServico o = new OrdemServico();
        o.setVeiculoId(veiculo.getId());
        o.setDefeito(dados.defeito().trim());
        o.setValor(BigDecimal.ZERO.setScale(2));   // valor "a definir" pela oficina
        o.setStatus(OrdemServico.SOLICITADA);
        Long id = ordemRepository.insert(o);
        return ordemRepository.findById(id).orElseThrow();
    }
}
