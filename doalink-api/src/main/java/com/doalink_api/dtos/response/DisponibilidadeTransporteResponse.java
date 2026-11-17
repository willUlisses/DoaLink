package com.doalink_api.dtos.response;

import com.doalink_api.entities.DisponibilidadeTransporte;
import com.doalink_api.entities.enums.SituacaoDisponibilidade;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.entities.enums.TipoVeiculo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record DisponibilidadeTransporteResponse(
        Long id,
        SituacaoDisponibilidade situacao,
        ContatoResponse transportador,
        TipoVeiculo tipoVeiculo,
        Double capacidade,
        Double capacidadeRestante,
        TipoUnidade tipoUnidade,
        EnderecoResponse origem,
        LocalDate dataDisponivel,
        LocalTime horarioDisponivel,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
    public static DisponibilidadeTransporteResponse from(DisponibilidadeTransporte d) {
        return new DisponibilidadeTransporteResponse(
                d.getId(),
                d.getSituacao(),
                d.getTransportador() != null ? ContatoResponse.from(d.getTransportador()) : null,
                d.getTipoVeiculo(),
                d.getCapacidade(),
                d.getCapacidadeRestante(),
                d.getTipoUnidade(),
                d.getOrigem() != null ? EnderecoResponse.from(d.getOrigem()) : null,
                d.getDataDisponivel(),
                d.getHorarioDisponivel(),
                d.getCriadoEm(),
                d.getAtualizadoEm()
        );
    }
}
