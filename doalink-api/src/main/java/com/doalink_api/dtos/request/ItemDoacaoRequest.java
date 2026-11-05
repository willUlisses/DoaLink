package com.doalink_api.dtos.request;

import com.doalink_api.entities.enums.TipoItemDoacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemDoacaoRequest(
        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Double quantidade,

        @NotNull(message = "O tipo do item é obrigatório")
        TipoItemDoacao tipoItemDoacao
) {
}

