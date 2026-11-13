package com.doalink_api.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificarEmailRequest(
        @NotBlank(message = "E-mail é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotBlank(message = "Código de verificação é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "Código de verificação deve conter exatamente 6 dígitos numéricos")
        String codigo
) {
}
