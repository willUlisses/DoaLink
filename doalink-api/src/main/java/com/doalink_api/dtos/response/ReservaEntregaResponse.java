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
        Long transportadorId,
        ContatoResponse doador,
        ContatoResponse receptor,
        ContatoResponse transportador,
        EnderecoResponse enderecoRetirada,
        EnderecoResponse enderecoEntrega,
        List<ItemDoacaoResponse> itens,
        LocalDateTime aceitoEm,
        LocalDateTime autorizadoEm,
        LocalDateTime retiradaConfirmadaEm,
        LocalDateTime entregaConfirmadaEm,
        String codigoRetirada,
        String codigoEntrega,
        LocalDateTime codigoEntregaExpiraEm,
        List<EventoTransporteResponse> historicoEventos,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static ReservaEntregaResponse from(ReservaEntrega reserva, boolean incluirDadosCompletos) {
        return from(reserva, incluirDadosCompletos, null, null, null, List.of());
    }

    public static ReservaEntregaResponse from(
            ReservaEntrega reserva,
            boolean incluirDadosCompletos,
            String codigoRetirada,
            String codigoEntrega,
            LocalDateTime codigoEntregaExpiraEm) {
        return from(reserva, incluirDadosCompletos, codigoRetirada, codigoEntrega, codigoEntregaExpiraEm, List.of());
    }

    public static ReservaEntregaResponse from(
            ReservaEntrega reserva,
            boolean incluirDadosCompletos,
            String codigoRetirada,
            String codigoEntrega,
            LocalDateTime codigoEntregaExpiraEm,
            List<EventoTransporteResponse> historicoEventos) {
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
        Long transportadorId = null;
        if (reserva.getTransportador() != null) {
            transportadorId = reserva.getTransportador().getId();
            transportadorContato = ContatoResponse.from(reserva.getTransportador());
        }

        return new ReservaEntregaResponse(
                reserva.getId(),
                reserva.getSituacao(),
                reserva.isPrecisaTransporte(),
                oferta != null ? oferta.getId() : null,
                oferta != null ? oferta.getSituacao() : null,
                transportadorId,
                doadorContato,
                receptorContato,
                transportadorContato,
                retirada,
                entrega,
                oferta != null ? oferta.getItens().stream().map(ItemDoacaoResponse::from).toList() : List.of(),
                reserva.getAceitoEm(),
                reserva.getAutorizadoEm(),
                reserva.getRetiradaConfirmadaEm(),
                reserva.getEntregaConfirmadaEm(),
                codigoRetirada,
                codigoEntrega,
                codigoEntregaExpiraEm,
                historicoEventos != null ? historicoEventos : List.of(),
                reserva.getCriadoEm(),
                reserva.getAtualizadoEm()
        );
    }
}
