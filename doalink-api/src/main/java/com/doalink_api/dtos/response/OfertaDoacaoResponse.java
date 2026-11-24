package com.doalink_api.dtos.response;

import com.doalink_api.entities.OfertaDoacao;
import com.doalink_api.entities.ReservaEntrega;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;

import java.time.LocalDateTime;
import java.util.List;

public record OfertaDoacaoResponse(
        Long id,
        SituacaoOferta situacao,
        ContatoResponse doador,
        ContatoResponse receptor,
        EnderecoResponse localRetirada,
        List<ItemDoacaoResponse> itens,
        Long reservaId,
        SituacaoReservaEntrega situacaoReserva,
        ContatoResponse transportador,
        String codigoRetirada,
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
        this(id, situacao, doador, null, localRetirada, itens, null, null, null, null, criadoEm, atualizadoEm);
    }

    public OfertaDoacaoResponse(
            Long id,
            SituacaoOferta situacao,
            ContatoResponse doador,
            ContatoResponse receptor,
            EnderecoResponse localRetirada,
            List<ItemDoacaoResponse> itens,
            LocalDateTime criadoEm,
            LocalDateTime atualizadoEm) {
        this(id, situacao, doador, receptor, localRetirada, itens, null, null, null, null, criadoEm, atualizadoEm);
    }

    public static OfertaDoacaoResponse from(OfertaDoacao oferta) {
        return from(oferta, true, null, null, null);
    }

    public static OfertaDoacaoResponse from(OfertaDoacao oferta, boolean incluirEnderecoCompleto) {
        return from(oferta, incluirEnderecoCompleto, null, null, null);
    }

    public static OfertaDoacaoResponse from(
            OfertaDoacao oferta,
            boolean incluirEnderecoCompleto,
            ContatoResponse receptor) {
        return from(oferta, incluirEnderecoCompleto, receptor, null, null);
    }

    public static OfertaDoacaoResponse from(
            OfertaDoacao oferta,
            boolean incluirEnderecoCompleto,
            ContatoResponse receptor,
            ReservaEntrega reservaAtiva,
            String codigoRetirada) {
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

        Long reservaId = reservaAtiva != null ? reservaAtiva.getId() : null;
        SituacaoReservaEntrega situacaoReserva = reservaAtiva != null ? reservaAtiva.getSituacao() : null;
        ContatoResponse transportadorContato = (incluirEnderecoCompleto && reservaAtiva != null && reservaAtiva.getTransportador() != null)
                ? ContatoResponse.from(reservaAtiva.getTransportador())
                : null;

        return new OfertaDoacaoResponse(
                oferta.getId(),
                oferta.getSituacao(),
                doadorContato,
                receptor,
                endereco,
                oferta.getItens().stream().map(ItemDoacaoResponse::from).toList(),
                reservaId,
                situacaoReserva,
                transportadorContato,
                codigoRetirada,
                oferta.getCriadoEm(),
                oferta.getAtualizadoEm()
        );
    }
}
