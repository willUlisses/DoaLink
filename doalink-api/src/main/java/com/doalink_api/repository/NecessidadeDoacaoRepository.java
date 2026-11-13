package com.doalink_api.repository;

import com.doalink_api.entities.NecessidadeDoacao;
import com.doalink_api.entities.enums.SituacaoNecessidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NecessidadeDoacaoRepository extends JpaRepository<NecessidadeDoacao, Long> {

    // Minhas necessidades — lista completa sem paginação (volume esperado por receptor é baixo)
    @EntityGraph(attributePaths = {"itens", "receptor"})
    List<NecessidadeDoacao> findByReceptorIdOrderByCriadoEmDesc(Long receptorId);

    // Consulta autenticada com filtro de situação — paginada
    @EntityGraph(attributePaths = {"itens", "receptor"})
    Page<NecessidadeDoacao> findBySituacao(SituacaoNecessidade situacao, Pageable pageable);

    // Busca por id com itens e receptor já carregados (evita N+1 ao montar o response)
    @EntityGraph(attributePaths = {"itens", "receptor"})
    Optional<NecessidadeDoacao> findWithItensById(Long id);
}
