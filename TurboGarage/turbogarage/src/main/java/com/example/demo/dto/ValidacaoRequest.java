package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Resposta do MECÂNICO (admin) a um agendamento (PUT /api/agendamentos/{id}/validacao).
 */
public record ValidacaoRequest(

        @NotBlank(message = "Escolha confirmar ou recusar")
        @Pattern(regexp = "CONFIRMADO|RECUSADO", message = "Status inválido")
        String status,

        @Size(max = 300, message = "Mensagem muito longa (máx. 300 caracteres)")
        String resposta
) {
}
