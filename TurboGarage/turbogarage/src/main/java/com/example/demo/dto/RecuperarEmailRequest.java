package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Recuperar o email: telefone + CEP que a pessoa usou no cadastro. */
public record RecuperarEmailRequest(
        @NotBlank(message = "Informe o telefone")
        @Pattern(regexp = "\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}", message = "Telefone inválido")
        String telefone,

        @NotBlank(message = "Informe o CEP")
        @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido")
        String cep
) {
}
