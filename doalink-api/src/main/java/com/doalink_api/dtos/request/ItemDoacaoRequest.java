package com.doalink_api.dtos.request;

import com.doalink_api.entities.enums.TipoItemDoacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemDoacaoRequest(
        @NotBlank(message = "A descrição do item é obrigatória")
        @Size(max = 500, message = "A descrição deve ter no máximo 100 caracteres")
        String descricao,

        @NotNull(message = "O tipo do item é obrigatório")
        TipoItemDoacao tipoItemDoacao,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Double quantidadeu

) {
}
