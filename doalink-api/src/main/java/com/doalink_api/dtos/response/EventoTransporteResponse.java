package com.doalink_api.dtos.response;

import com.doalink_api.entities.EventoTransporte;
import com.doalink_api.entities.enums.TipoEventoTransporte;

import java.time.LocalDateTime;

public record EventoTransporteResponse(
        Long id,
        TipoEventoTransporte tipoEvento,
        String detalhes,
        LocalDateTime criadoEm
) {
    public static EventoTransporteResponse from(EventoTransporte evento) {
        return new EventoTransporteResponse(
                evento.getId(),
                evento.getTipoEvento(),
                evento.getDetalhes(),
                evento.getCriadoEm()
        );
    }
}
