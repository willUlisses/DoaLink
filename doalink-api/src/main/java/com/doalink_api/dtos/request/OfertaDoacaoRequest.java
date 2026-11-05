package com.doalink_api.dtos.request;

import com.doalink_api.entities.enums.TipoItemDoacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record OfertaDoacaoRequest(
        @NotNull @Positive double quantidade,
        @NotNull TipoItemDoacao itemDoacao,
        @NotNull @Valid EnderecoRequest endereco
) {
}
