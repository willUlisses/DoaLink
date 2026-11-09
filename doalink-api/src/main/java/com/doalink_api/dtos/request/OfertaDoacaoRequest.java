package com.doalink_api.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OfertaDoacaoRequest(
        @NotEmpty(message = "A oferta deve conter ao menos um item de doação")
        @Size(max = 50, message = "A oferta pode ter no máximo 50 itens")
        @Valid
        List<ItemDoacaoRequest> itens,

        // Opcional: endereço do doador ou local de coleta do receptor
        @Valid
        EnderecoRequest endereco
) {
}
