package com.doalink_api.dtos.response;

import com.doalink_api.entities.Endereco;
import com.doalink_api.entities.enums.TipoItemDoacao;

public record OfertaDoacaoResponse(
        Long id,
        double quantidade,
        TipoItemDoacao categoria,
        Endereco endereco
) {

    public static OfertaDoacaoResponse from(OfertaDoacao o) {
        return new OfertaDoacaoResponse(
                o.getId(),
                o.getQuantidade(),
                o.getCategoria(),
                EnderecoResponse.from(o.getEndereco())
        );
    }

}