package com.example.demo.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Dados que a tela de Estoque envia em POST e PUT /api/pecas.
 */
public record PecaRequest(

        @NotBlank(message = "Código é obrigatório")
        @Size(max = 30, message = "Código muito longo")
        String codigo,

        @NotBlank(message = "Nome da peça é obrigatório")
        @Size(max = 120, message = "Nome muito longo")
        String nome,

        @Size(max = 60, message = "Categoria muito longa")
        String categoria,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 0, message = "A quantidade não pode ser negativa")
        @Max(value = 1000000, message = "Quantidade muito alta")
        Integer quantidade,

        @NotNull(message = "Estoque mínimo é obrigatório")
        @Min(value = 0, message = "O estoque mínimo não pode ser negativo")
        Integer estoqueMinimo,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
        @DecimalMax(value = "99999999.99", message = "Preço muito alto")
        BigDecimal preco
) {
}
