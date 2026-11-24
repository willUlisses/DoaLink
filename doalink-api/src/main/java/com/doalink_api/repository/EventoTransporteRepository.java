package com.doalink_api.repository;

import com.doalink_api.entities.EventoTransporte;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoTransporteRepository extends JpaRepository<EventoTransporte, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    List<EventoTransporte> findByReservaIdOrderByCriadoEmAsc(Long reservaId);
}
