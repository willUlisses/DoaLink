package com.doalink_api.repository;

import com.doalink_api.entities.OcorrenciaTransporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OcorrenciaTransporteRepository extends JpaRepository<OcorrenciaTransporte, Long> {

    boolean existsByReservaId(Long reservaId);

    Optional<OcorrenciaTransporte> findByReservaId(Long reservaId);

    List<OcorrenciaTransporte> findByTransportadorIdOrderByCriadoEmDesc(Long transportadorId);
}
