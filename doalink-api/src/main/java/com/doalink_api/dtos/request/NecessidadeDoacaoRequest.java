package com.doalink_api.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NecessidadeDoacaoRequest(
        @NotEmpty(message = "A necessidade deve conter ao menos um item")
        @Size(max = 50, message = "A necessidade pode ter no máximo 50 itens")
        @Valid
        List<ItemNecessidadeRequest> itens,

        @NotNull(message = "O local de entrega é obrigatório")
        @Valid
        EnderecoRequest localEntrega
) {
}
