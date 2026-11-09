package com.doalink_api.dtos.response;

import com.doalink_api.entities.OfertaDoacao;
import com.doalink_api.entities.enums.SituacaoOferta;

import java.time.LocalDateTime;
import java.util.List;

public record OfertaDoacaoResponse(
        Long id,
        SituacaoOferta situacao,
        ContatoResponse doador,
        EnderecoResponse localRetirada,
        List<ItemDoacaoResponse> itens,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static OfertaDoacaoResponse from(OfertaDoacao oferta) {
        return new OfertaDoacaoResponse(
                oferta.getId(),
                oferta.getSituacao(),
                oferta.getDoador() != null ? ContatoResponse.from(oferta.getDoador()) : null,
                oferta.getLocalRetirada() != null ? EnderecoResponse.from(oferta.getLocalRetirada()) : null,
                oferta.getItens().stream().map(ItemDoacaoResponse::from).toList(),
                oferta.getCriadoEm(),
                oferta.getAtualizadoEm()
        );
    }
}
