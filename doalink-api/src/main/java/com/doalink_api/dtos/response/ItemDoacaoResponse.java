package com.doalink_api.dtos.response;

import com.doalink_api.entities.ItemDoacao;
import com.doalink_api.entities.enums.TipoItemDoacao;

import java.math.BigDecimal;

public record ItemDoacaoResponse(
        Long id,
        String descricao,
        TipoItemDoacao tipoItemDoacao,
        BigDecimal quantidade
) {
    public static ItemDoacaoResponse from(ItemDoacao item) {
        return new ItemDoacaoResponse(
                item.getId(),
                item.getDescricao(),
                item.getTipoItemDoacao(),
                item.getQuantidade()
        );
    }
}
