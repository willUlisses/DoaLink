package com.doalink_api.dtos.response;

import com.doalink_api.entities.NecessidadeDoacao;
import com.doalink_api.entities.enums.SituacaoNecessidade;

import java.time.LocalDateTime;
import java.util.List;

public record NecessidadeDoacaoResponse(
        Long id,
        SituacaoNecessidade situacao,
        ContatoResponse receptor,
        EnderecoResponse localEntrega,
        List<ItemNecessidadeResponse> itens,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static NecessidadeDoacaoResponse from(NecessidadeDoacao necessidade) {
        return new NecessidadeDoacaoResponse(
                necessidade.getId(),
                necessidade.getSituacao(),
                ContatoResponse.from(necessidade.getReceptor()),
                EnderecoResponse.from(necessidade.getLocalEntrega()),
                necessidade.getItens().stream().map(ItemNecessidadeResponse::from).toList(),
                necessidade.getCriadoEm(),
                necessidade.getAtualizadoEm()
        );
    }
}
