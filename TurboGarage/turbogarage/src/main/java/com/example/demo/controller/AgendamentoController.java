package com.example.demo.controller;

import com.example.demo.dto.ValidacaoRequest;
import com.example.demo.model.Agendamento;
import com.example.demo.service.AgendamentoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * CONTROLLER: agendamentos do lado do MECÂNICO (só admin, protegido pelo AdminInterceptor).
 *   GET /api/agendamentos                  -> todos os agendamentos
 *   PUT /api/agendamentos/{id}/validacao   -> confirmar ou recusar
 */
@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService service;

    public AgendamentoController(AgendamentoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Agendamento> listar() {
        return service.listarTodos();
    }

    @PutMapping("/{id}/validacao")
    public Map<String, Object> validar(@PathVariable Long id, @Valid @RequestBody ValidacaoRequest dados) {
        Agendamento a = service.validar(id, dados);
        String msg = Agendamento.CONFIRMADO.equals(a.getStatus()) ? "Agendamento confirmado" : "Agendamento recusado";
        return Map.of("mensagem", msg, "registro", a);
    }
}
