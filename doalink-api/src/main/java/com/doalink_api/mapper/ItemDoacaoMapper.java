package com.doalink_api.mapper;

import com.doalink_api.dtos.request.ItemDoacaoRequest;
import com.doalink_api.entities.ItemDoacao;

public class ItemDoacaoMapper {

    private ItemDoacaoMapper() {}

    public static ItemDoacao toEntity(ItemDoacaoRequest req) {
        return new ItemDoacao(
                req.descricao().trim(),
                req.tipoItemDoacao(),
                req.quantidade()
        );
    }
}
