package com.doalink_api.dtos.response;

import com.doalink_api.entities.OfertaDoacao;
import com.doalink_api.entities.ReservaEntrega;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;

import java.time.LocalDateTime;
import java.util.List;

public record ReservaEntregaResponse(
        Long id,
        SituacaoReservaEntrega situacao,
        boolean precisaTransporte,
        Long ofertaId,
        SituacaoOferta situacaoOferta,
        Long disponibilidadeId,
        ContatoResponse doador,
        ContatoResponse receptor,
        ContatoResponse transportador,
        EnderecoResponse enderecoRetirada,
        EnderecoResponse enderecoEntrega,
        List<ItemDoacaoResponse> itens,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static ReservaEntregaResponse from(ReservaEntrega reserva, boolean incluirDadosCompletos) {
        OfertaDoacao oferta = reserva.getOferta();
        EnderecoResponse retirada = null;
        if (oferta != null && oferta.getLocalRetirada() != null) {
            retirada = incluirDadosCompletos
                    ? EnderecoResponse.from(oferta.getLocalRetirada())
                    : EnderecoResponse.resumido(oferta.getLocalRetirada());
        }

        EnderecoResponse entrega = null;
        if (reserva.getReceptor() != null && reserva.getReceptor().getEnderecoOrganizacao() != null) {
            entrega = incluirDadosCompletos
                    ? EnderecoResponse.from(reserva.getReceptor().getEnderecoOrganizacao())
                    : EnderecoResponse.resumido(reserva.getReceptor().getEnderecoOrganizacao());
        }

        ContatoResponse doadorContato = incluirDadosCompletos && oferta != null && oferta.getDoador() != null
                ? ContatoResponse.from(oferta.getDoador())
                : null;

        ContatoResponse receptorContato = incluirDadosCompletos && reserva.getReceptor() != null
                ? ContatoResponse.from(reserva.getReceptor())
                : null;

        ContatoResponse transportadorContato = null;
        Long disponibilidadeId = null;
        if (reserva.getDisponibilidade() != null) {
            disponibilidadeId = reserva.getDisponibilidade().getId();
            if (reserva.getDisponibilidade().getTransportador() != null) {
                transportadorContato = ContatoResponse.from(reserva.getDisponibilidade().getTransportador());
            }
        }

        return new ReservaEntregaResponse(
                reserva.getId(),
                reserva.getSituacao(),
                reserva.isPrecisaTransporte(),
                oferta != null ? oferta.getId() : null,
                oferta != null ? oferta.getSituacao() : null,
                disponibilidadeId,
                doadorContato,
                receptorContato,
                transportadorContato,
                retirada,
                entrega,
                oferta != null ? oferta.getItens().stream().map(ItemDoacaoResponse::from).toList() : List.of(),
                reserva.getCriadoEm(),
                reserva.getAtualizadoEm()
        );
    }
}

