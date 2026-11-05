package com.doalink_api.dtos.response;

import com.doalink_api.entities.Endereco;
import com.doalink_api.entities.ItemDoacao;
import com.doalink_api.entities.enums.TipoItemDoacao;

public record OfertaDoacaoResponse(
        Long id,
        double quantidade,
        TipoItemDoacao categoria,
        Endereco endereco
) {

    public static OfertaDoacaoResponse from(ItemDoacao i) {
        return new OfertaDoacaoResponse(
                i.getId(),
                i.getQuantidade(),
                i.getCategoria(),
                EnderecoResponse.from(i.getEndereco())
        );
    }

}