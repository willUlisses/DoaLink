package com.doalink_api.dtos.response;

import com.doalink_api.entities.ItemDoacao;
import com.doalink_api.entities.enums.TipoItemDoacao;

public record ItemDoacaoResponse(
        Long id,
        Double quantidade,
        TipoItemDoacao tipoItemDoacao
) {
    public static ItemDoacaoResponse from(ItemDoacao item) {
        return new ItemDoacaoResponse(
                item.getId(),
                item.getQuantidade(),
                item.getTipoItemDoacao()
        );
    }
}

