package com.doalink_api.repository;

import com.doalink_api.entities.CodigoTransporte;
import com.doalink_api.entities.enums.TipoCodigoTransporte;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CodigoTransporteRepository extends JpaRepository<CodigoTransporte, Long> {

    Optional<CodigoTransporte> findFirstByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(
            Long reservaId, TipoCodigoTransporte tipo);

    Optional<CodigoTransporte> findFirstByReservaIdAndTipoOrderByCriadoEmDesc(
            Long reservaId, TipoCodigoTransporte tipo);

    List<CodigoTransporte> findByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalse(
            Long reservaId, TipoCodigoTransporte tipo);

    List<CodigoTransporte> findByReservaIdInAndTipoAndInvalidadoFalseAndUtilizadoFalse(
            Collection<Long> reservaIds, TipoCodigoTransporte tipo);

    @EntityGraph(attributePaths = {"reserva", "reserva.oferta", "reserva.oferta.doador", "reserva.receptor", "transportador"})
    List<CodigoTransporte> findByTipoAndUtilizadoFalseAndInvalidadoFalseAndExpiraEmBefore(
            TipoCodigoTransporte tipo, LocalDateTime agora);
}
