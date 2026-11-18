package com.example.demo.controller;

import com.example.demo.dto.NovaSenhaRequest;
import com.example.demo.dto.PedidoCodigoRequest;
import com.example.demo.dto.RecuperarEmailRequest;
import com.example.demo.service.RecuperacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * CONTROLLER: recuperação de senha e de email (rotas PÚBLICAS: quem esqueceu não está logado).
 *   POST /api/recuperar/senha/codigo  -> envia o código de 6 dígitos
 *   POST /api/recuperar/senha/nova    -> confere o código e grava a senha nova
 *   POST /api/recuperar/email         -> telefone + CEP -> email com parte escondida
 */
@RestController
@RequestMapping("/api/recuperar")
public class RecuperacaoController {

    private final RecuperacaoService service;

    public RecuperacaoController(RecuperacaoService service) {
        this.service = service;
    }

    @PostMapping("/senha/codigo")
    public Map<String, String> pedirCodigo(@Valid @RequestBody PedidoCodigoRequest dados) {
        service.pedirCodigo(dados.email());
        // Mesma resposta exista o email ou não
        return Map.of("mensagem", "Se esse email estiver cadastrado, enviamos um código de 6 números. Ele vale por 15 minutos.");
    }

    @PostMapping("/senha/nova")
    public Map<String, String> trocarSenha(@Valid @RequestBody NovaSenhaRequest dados) {
        service.trocarSenha(dados);
        return Map.of("mensagem", "Senha alterada com sucesso! Já pode entrar com a senha nova.");
    }

    @PostMapping("/email")
    public Map<String, String> recuperarEmail(@Valid @RequestBody RecuperarEmailRequest dados) {
        String email = service.recuperarEmail(dados);
        return Map.of("mensagem", "Encontramos seu cadastro. Seu email é: " + email, "email", email);
    }
}
