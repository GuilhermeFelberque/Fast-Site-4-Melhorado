package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Entrada (chegou peça) ou saída (usou peça) no estoque: POST /api/pecas/{id}/movimento.
 */
public record MovimentoRequest(

        @NotBlank(message = "Informe se é entrada ou saída")
        @Pattern(regexp = "ENTRADA|SAIDA", message = "Tipo inválido")
        String tipo,

        @NotNull(message = "Informe a quantidade")
        @Min(value = 1, message = "A quantidade deve ser pelo menos 1")
        @Max(value = 100000, message = "Quantidade muito alta")
        Integer quantidade
) {
}
