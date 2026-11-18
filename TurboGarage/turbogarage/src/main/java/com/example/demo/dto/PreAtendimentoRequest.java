package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados que o CLIENTE envia para pedir um pré-atendimento
 * (POST /api/cliente/pre-atendimento).
 * O valor e o status NÃO vêm do cliente: quem define é a oficina.
 */
public record PreAtendimentoRequest(

        @NotNull(message = "Escolha o veículo")
        Long veiculoId,

        @NotBlank(message = "Descreva o problema do veículo")
        @Size(max = 500, message = "Descrição muito longa (máx. 500 caracteres)")
        String defeito
) {
}
