package com.example.demo.controller;

import com.example.demo.dto.MovimentoRequest;
import com.example.demo.dto.PecaRequest;
import com.example.demo.model.Peca;
import com.example.demo.service.PecaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER: estoque de peças (só admin, protegido pelo AdminInterceptor).
 *   GET    /api/pecas                 -> lista
 *   GET    /api/pecas/{id}            -> uma
 *   POST   /api/pecas                 -> cadastra
 *   PUT    /api/pecas/{id}            -> atualiza
 *   DELETE /api/pecas/{id}            -> exclui
 *   POST   /api/pecas/{id}/movimento  -> entrada ou saída de quantidade
 */
@RestController
@RequestMapping("/api/pecas")
public class PecaController {

    private final PecaService service;

    public PecaController(PecaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Peca> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Peca buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> criar(@Valid @RequestBody PecaRequest dados) {
        Peca p = service.criar(dados);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensagem", "Registro salvo com sucesso!", "registro", p));
    }

    @PutMapping("/{id}")
    public Map<String, Object> atualizar(@PathVariable Long id, @Valid @RequestBody PecaRequest dados) {
        return Map.of("mensagem", "Registro atualizado", "registro", service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> excluir(@PathVariable Long id) {
        service.excluir(id);
        return Map.of("mensagem", "Registro excluído");
    }

    @PostMapping("/{id}/movimento")
    public Map<String, Object> movimentar(@PathVariable Long id, @Valid @RequestBody MovimentoRequest dados) {
        Peca p = service.movimentar(id, dados);
        String msg = ("ENTRADA".equals(dados.tipo()) ? "Entrada" : "Saída") + " registrada: " + p.getNome()
                + " agora tem " + p.getQuantidade() + " unidade(s)";
        return Map.of("mensagem", msg, "registro", p);
    }
}
