package com.doalink_api.mapper;

import com.doalink_api.dtos.request.DisponibilidadeTransporteRequest;
import com.doalink_api.entities.DisponibilidadeTransporte;
import com.doalink_api.entities.Transportador;

public class DisponibilidadeTransporteMapper {

    private DisponibilidadeTransporteMapper() {
    }

    public static DisponibilidadeTransporte toEntity(DisponibilidadeTransporteRequest req, Transportador transportador) {
        DisponibilidadeTransporte d = new DisponibilidadeTransporte();
        d.setTransportador(transportador);
        d.setTipoVeiculo(req.tipoVeiculo());
        d.setCapacidade(req.capacidade());
        d.setCapacidadeRestante(req.capacidade());
        d.setTipoUnidade(req.tipoUnidade());
        d.setOrigem(EnderecoMapper.toEndereco(req.origem()));
        d.setDataDisponivel(req.dataDisponivel());
        d.setHorarioDisponivel(req.horarioDisponivel());
        return d;
    }
}
