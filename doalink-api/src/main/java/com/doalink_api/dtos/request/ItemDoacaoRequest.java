package com.doalink_api.dtos.request;

import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ItemDoacaoRequest(
        @NotBlank(message = "A descrição do item é obrigatória")
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String descricao,

        @NotNull(message = "O tipo do item é obrigatório")
        TipoItemDoacao tipoItemDoacao,

        @NotNull(message = "A unidade do item é obrigatória")
        TipoUnidade tipoUnidade,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Double quantidade
) {
}
