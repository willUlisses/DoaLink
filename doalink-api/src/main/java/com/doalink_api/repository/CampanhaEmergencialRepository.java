package com.doalink_api.repository;

import com.doalink_api.entities.CampanhaEmergencial;
import com.doalink_api.entities.enums.SituacaoCampanha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampanhaEmergencialRepository extends JpaRepository<CampanhaEmergencial, Long> {

    List<CampanhaEmergencial> findByReceptorIdOrderByCriadoEmDesc(Long receptorId);

    Page<CampanhaEmergencial> findBySituacao(SituacaoCampanha situacao, Pageable pageable);

    @EntityGraph(attributePaths = {"itens", "receptor"})
    Optional<CampanhaEmergencial> findWithItensById(Long id);
}
