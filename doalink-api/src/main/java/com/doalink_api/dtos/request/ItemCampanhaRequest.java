package com.doalink_api.dtos.request;

import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ItemCampanhaRequest(
        @NotNull(message = "O tipo do item é obrigatório")
        TipoItemDoacao tipoItemDoacao,

        @NotNull(message = "A unidade de medida é obrigatória")
        TipoUnidade tipoUnidade,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Double quantidade,

        @Size(max = 500, message = "A descrição do item deve ter no máximo 500 caracteres")
        String descricao
) {
}
