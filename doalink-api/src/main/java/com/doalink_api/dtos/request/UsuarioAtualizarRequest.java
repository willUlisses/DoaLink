package com.doalink_api.dtos.request;

import jakarta.validation.Valid;

public record UsuarioAtualizarRequest(
        @Valid
        EnderecoRequest endereco
) {
}
