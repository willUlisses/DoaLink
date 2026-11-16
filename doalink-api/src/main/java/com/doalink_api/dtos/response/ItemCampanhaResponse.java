package com.doalink_api.dtos.response;

import com.doalink_api.entities.ItemCampanha;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;

public record ItemCampanhaResponse(
        Long id,
        TipoItemDoacao tipoItemDoacao,
        TipoUnidade tipoUnidade,
        Double quantidade,
        String descricao
) {
    public static ItemCampanhaResponse from(ItemCampanha item) {
        return new ItemCampanhaResponse(
                item.getId(),
                item.getTipoItemDoacao(),
                item.getTipoUnidade(),
                item.getQuantidade(),
                item.getDescricao()
        );
    }
}
