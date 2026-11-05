package com.doalink_api.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OfertaDoacaoRequest(
        @NotEmpty(message = "A oferta deve conter ao menos um item de doação")
        @Valid
        List<ItemDoacaoRequest> itens,

        @NotNull(message = "O endereço de coleta é obrigatório")
        @Valid
        EnderecoRequest endereco
) {
}
