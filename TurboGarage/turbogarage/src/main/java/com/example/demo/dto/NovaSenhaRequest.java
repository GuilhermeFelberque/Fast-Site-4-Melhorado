package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Passo 2 da recuperação de senha: código recebido + senha nova. */
public record NovaSenhaRequest(
        @NotBlank(message = "Informe o email")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "Informe o código")
        @Pattern(regexp = "\\d{6}", message = "O código tem 6 números")
        String codigo,

        @NotBlank(message = "Informe a nova senha")
        @Size(min = 6, max = 72, message = "A senha precisa ter de 6 a 72 caracteres")
        String novaSenha
) {
}
