package com.doalink_api.dtos.response;

import com.doalink_api.entities.OfertaDoacao;
import com.doalink_api.entities.enums.SituacaoOferta;

import java.time.LocalDateTime;
import java.util.List;

public record OfertaDoacaoResponse(
        Long id,
        SituacaoOferta situacao,
        ContatoResponse doador,
        ContatoResponse receptor,
        EnderecoResponse localRetirada,
        List<ItemDoacaoResponse> itens,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public OfertaDoacaoResponse(
            Long id,
            SituacaoOferta situacao,
            ContatoResponse doador,
            EnderecoResponse localRetirada,
            List<ItemDoacaoResponse> itens,
            LocalDateTime criadoEm,
            LocalDateTime atualizadoEm) {
        this(id, situacao, doador, null, localRetirada, itens, criadoEm, atualizadoEm);
    }

    public static OfertaDoacaoResponse from(OfertaDoacao oferta) {
        return from(oferta, true, null);
    }

    public static OfertaDoacaoResponse from(OfertaDoacao oferta, boolean incluirEnderecoCompleto) {
        return from(oferta, incluirEnderecoCompleto, null);
    }

    public static OfertaDoacaoResponse from(
            OfertaDoacao oferta,
            boolean incluirEnderecoCompleto,
            ContatoResponse receptor) {
        EnderecoResponse endereco = null;
        if (oferta.getLocalRetirada() != null) {
            endereco = incluirEnderecoCompleto
                    ? EnderecoResponse.from(oferta.getLocalRetirada())
                    : EnderecoResponse.resumido(oferta.getLocalRetirada());
        }
        ContatoResponse doadorContato = null;
        if (oferta.getDoador() != null) {
            doadorContato = incluirEnderecoCompleto
                    ? ContatoResponse.from(oferta.getDoador())
                    : new ContatoResponse(oferta.getDoador().getNome(), null, null);
        }
        return new OfertaDoacaoResponse(
                oferta.getId(),
                oferta.getSituacao(),
                doadorContato,
                receptor,
                endereco,
                oferta.getItens().stream().map(ItemDoacaoResponse::from).toList(),
                oferta.getCriadoEm(),
                oferta.getAtualizadoEm()
        );
    }
}
