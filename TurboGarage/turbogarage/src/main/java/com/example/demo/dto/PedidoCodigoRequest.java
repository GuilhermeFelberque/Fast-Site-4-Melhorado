package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Passo 1 da recuperação de senha: o email para onde vai o código. */
public record PedidoCodigoRequest(
        @NotBlank(message = "Informe o email")
        @Email(message = "Email inválido")
        String email
) {
}
