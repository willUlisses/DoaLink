package com.doalink_api.repository;

import com.doalink_api.entities.OfertaDoacao;
import com.doalink_api.entities.enums.SituacaoOferta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfertaDoacaoRepository extends JpaRepository<OfertaDoacao, Long> {

    // Minhas ofertas — lista completa sem paginação (volume esperado por doador é baixo)
    @EntityGraph(attributePaths = "itens")
    List<OfertaDoacao> findByDoadorIdOrderByCriadoEmDesc(Long doadorId);

    // Consulta pública com filtro de situação — paginada (CA-004-2)
    @EntityGraph(attributePaths = "itens")
    Page<OfertaDoacao> findBySituacao(SituacaoOferta situacao, Pageable pageable);

    // Busca por id com itens já carregados (evita N+1 ao montar o response)
    @EntityGraph(attributePaths = "itens")
    Optional<OfertaDoacao> findWithItensById(Long id);
}
