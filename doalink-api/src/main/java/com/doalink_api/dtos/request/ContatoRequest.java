package com.doalink_api.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContatoRequest(
        @Email(message = "Informe um endereço de e-mail válido")
        @Size(max = 254, message = "O e-mail deve ter no máximo 254 caracteres")
        String email,

        @Pattern(regexp = "^$|^[0-9()\\s+-]{8,20}$", message = "Informe um telefone válido")
        String telefone
) {
}
