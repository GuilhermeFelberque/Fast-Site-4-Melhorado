package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Pedido de agendamento do CLIENTE (POST /api/cliente/agendamentos).
 * Data e hora chegam como texto ("2026-11-20" e "09:00") e o Service converte.
 */
public record AgendamentoRequest(

        @NotNull(message = "Escolha o veículo")
        Long veiculoId,

        @NotBlank(message = "Escolha o dia")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Data inválida")
        String data,

        @NotBlank(message = "Escolha o horário")
        @Pattern(regexp = "\\d{2}:\\d{2}", message = "Horário inválido")
        String hora,

        @Size(max = 500, message = "Motivo muito longo (máx. 500 caracteres)")
        String motivo
) {
}
