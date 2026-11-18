package com.example.demo.service;

import com.example.demo.dto.RelatorioOS;
import com.example.demo.model.Cadastro;
import com.example.demo.model.OrdemServico;
import com.example.demo.model.Veiculo;
import com.example.demo.repository.CadastroRepository;
import com.example.demo.repository.OrdemServicoRepository;
import com.example.demo.repository.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * SERVICE: relatório da ordem de serviço.
 * Só existe relatório para o carro que JÁ FOI ARRUMADO (status CONCLUIDA).
 *  - admin: relatório de qualquer ordem concluída
 *  - cliente: só das ordens dos carros DELE
 */
@Service
public class RelatorioService {

    private final OrdemServicoRepository ordemRepository;
    private final VeiculoRepository veiculoRepository;
    private final CadastroRepository cadastroRepository;

    public RelatorioService(OrdemServicoRepository ordemRepository, VeiculoRepository veiculoRepository,
                            CadastroRepository cadastroRepository) {
        this.ordemRepository = ordemRepository;
        this.veiculoRepository = veiculoRepository;
        this.cadastroRepository = cadastroRepository;
    }

    // ADMIN
    public RelatorioOS gerar(Long ordemId) {
        OrdemServico o = ordemConcluida(ordemId);
        Veiculo v = veiculoDa(o);
        Cadastro c = cadastroRepository.findById(v.getClienteId()).orElse(null);
        return montar(o, v, c);
    }

    // CLIENTE: confere se o carro é dele
    public RelatorioOS gerarParaCliente(String emailLogado, Long ordemId) {
        Cadastro meu = cadastroRepository.findAtivoByEmail(emailLogado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Seu login não tem um cadastro de cliente ativo"));
        OrdemServico o = ordemRepository.findById(ordemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço #" + ordemId + " não encontrada"));
        Veiculo v = veiculoDa(o);
        if (!v.getClienteId().equals(meu.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Esta ordem de serviço não é de um veículo seu");
        }
        ordemConcluida(ordemId); // 409 se ainda não foi concluída
        return montar(o, v, meu);
    }

    private OrdemServico ordemConcluida(Long id) {
        OrdemServico o = ordemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ordem de serviço #" + id + " não encontrada"));
        if (!OrdemServico.CONCLUIDA.equals(o.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Relatório disponível só para serviço concluído (o carro ainda não foi arrumado)");
        }
        return o;
    }

    private Veiculo veiculoDa(OrdemServico o) {
        return veiculoRepository.findById(o.getVeiculoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado"));
    }

    private RelatorioOS montar(OrdemServico o, Veiculo v, Cadastro c) {
        String endereco = c == null ? null
                : c.getEndereco() + ", " + c.getNumero() + " - CEP " + c.getCep();
        String emitido = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
        return new RelatorioOS(
                o.getId(), o.getStatus(), o.getDataAbertura(), o.getDataConclusao(), emitido,
                o.getDefeito(), o.getValor(),
                c == null ? null : c.getNome(), c == null ? null : c.getTelefone(),
                c == null ? null : c.getEmail(), endereco,
                v.getPlaca(), v.getModelo(), v.getAno(), v.getObservacao());
    }
}
