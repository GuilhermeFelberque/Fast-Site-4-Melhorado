package com.example.demo.service;

import com.example.demo.dto.MovimentoRequest;
import com.example.demo.dto.PecaRequest;
import com.example.demo.model.Peca;
import com.example.demo.repository.PecaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.util.List;

/**
 * SERVICE: regras do estoque de peças.
 *  - o código da peça não pode repetir
 *  - a saída não pode deixar o estoque negativo
 */
@Service
public class PecaService {

    private final PecaRepository repository;

    public PecaService(PecaRepository repository) {
        this.repository = repository;
    }

    public List<Peca> listar() {
        return repository.findAll();
    }

    public Peca buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Peça #" + id + " não encontrada"));
    }

    public Peca criar(PecaRequest dados) {
        Long id = repository.insert(montar(dados, null));
        return buscar(id);
    }

    public Peca atualizar(Long id, PecaRequest dados) {
        buscar(id);
        repository.update(id, montar(dados, id));
        return buscar(id);
    }

    public void excluir(Long id) {
        buscar(id);
        repository.delete(id);
    }

    // Entrada soma, saída subtrai
    public Peca movimentar(Long id, MovimentoRequest dados) {
        Peca p = buscar(id);
        int diferenca = "ENTRADA".equals(dados.tipo()) ? dados.quantidade() : -dados.quantidade();
        if (repository.movimentar(id, diferenca) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Estoque insuficiente: só há " + p.getQuantidade() + " unidade(s) de " + p.getNome());
        }
        return buscar(id);
    }

    private Peca montar(PecaRequest dados, Long idAtual) {
        String codigo = dados.codigo().trim().toUpperCase();
        if (repository.existsCodigo(codigo, idAtual)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe uma peça com o código " + codigo);
        }
        Peca p = new Peca();
        p.setCodigo(codigo);
        p.setNome(dados.nome().trim());
        String categoria = dados.categoria() == null ? "" : dados.categoria().trim();
        p.setCategoria(categoria.isEmpty() ? null : categoria);
        p.setQuantidade(dados.quantidade());
        p.setEstoqueMinimo(dados.estoqueMinimo());
        p.setPreco(dados.preco().setScale(2, RoundingMode.HALF_UP));
        return p;
    }
}
