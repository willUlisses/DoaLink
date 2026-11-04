package com.doalink_api.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;

public record CadastroReceptorRequest(
        @NotBlank @CNPJ String cnpj,
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String senha,
        @NotNull @Valid EnderecoRequest endereco
) {
}
