package com.doalink_api.dtos.response;

import com.doalink_api.entities.CampanhaEmergencial;
import com.doalink_api.entities.enums.SituacaoCampanha;

import java.time.LocalDateTime;
import java.util.List;

public record CampanhaEmergencialResponse(
        Long id,
        String nome,
        String descricao,
        SituacaoCampanha situacao,
        ContatoResponse receptor,
        EnderecoResponse enderecoRecebimento,
        List<ItemCampanhaResponse> itens,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static CampanhaEmergencialResponse from(CampanhaEmergencial campanha) {
        return new CampanhaEmergencialResponse(
                campanha.getId(),
                campanha.getNome(),
                campanha.getDescricao(),
                campanha.getSituacao(),
                campanha.getReceptor() != null ? ContatoResponse.from(campanha.getReceptor()) : null,
                campanha.getEnderecoRecebimento() != null ? EnderecoResponse.from(campanha.getEnderecoRecebimento()) : null,
                campanha.getItens() != null
                        ? campanha.getItens().stream().map(ItemCampanhaResponse::from).toList()
                        : List.of(),
                campanha.getCriadoEm(),
                campanha.getAtualizadoEm()
        );
    }
}
