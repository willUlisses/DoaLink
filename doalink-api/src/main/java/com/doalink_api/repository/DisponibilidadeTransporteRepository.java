package com.doalink_api.repository;

import com.doalink_api.entities.DisponibilidadeTransporte;
import com.doalink_api.entities.enums.SituacaoDisponibilidade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadeTransporteRepository extends JpaRepository<DisponibilidadeTransporte, Long> {

    boolean existsByTransportadorIdAndDataDisponivelAndSituacaoIn(
            Long transportadorId,
            LocalDate dataDisponivel,
            Collection<SituacaoDisponibilidade> situacoes
    );

    @EntityGraph(attributePaths = "transportador")
    List<DisponibilidadeTransporte> findByTransportadorIdOrderByDataDisponivelAscCriadoEmDesc(Long transportadorId);

    @EntityGraph(attributePaths = "transportador")
    Optional<DisponibilidadeTransporte> findWithTransportadorById(Long id);

    boolean existsByTransportadorId(Long transportadorId);
}

