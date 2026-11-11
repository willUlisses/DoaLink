package com.doalink_api.dtos.response;

import com.doalink_api.entities.ItemDoacao;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;

public record ItemNecessidadeResponse(
        Long id,
        String descricao,
        TipoItemDoacao tipoItemDoacao,
        TipoUnidade tipoUnidade,
        Double quantidade
) {
    public static ItemNecessidadeResponse from(ItemDoacao item) {
        return new ItemNecessidadeResponse(
                item.getId(),
                item.getDescricao(),
                item.getTipoItemDoacao(),
                item.getTipoUnidade(),
                item.getQuantidade()
        );
    }
}
