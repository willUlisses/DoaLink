package com.doalink_api.repository;

import com.doalink_api.entities.ReservaEntrega;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservaEntregaRepository extends JpaRepository<ReservaEntrega, Long> {

    @EntityGraph(attributePaths = {"oferta", "oferta.doador", "oferta.itens", "receptor", "transportador"})
    List<ReservaEntrega> findBySituacaoOrderByCriadoEmDesc(SituacaoReservaEntrega situacao);

    @EntityGraph(attributePaths = {"oferta", "oferta.doador", "oferta.itens", "receptor", "transportador"})
    List<ReservaEntrega> findByReceptorIdOrderByCriadoEmDesc(Long receptorId);

    @EntityGraph(attributePaths = {"oferta", "oferta.doador", "oferta.itens", "receptor", "transportador"})
    List<ReservaEntrega> findByTransportadorIdOrderByCriadoEmDesc(Long transportadorId);

    @EntityGraph(attributePaths = {"oferta", "oferta.doador", "oferta.itens", "receptor", "transportador"})
    List<ReservaEntrega> findByTransportadorIdAndSituacaoOrderByCriadoEmDesc(Long transportadorId, SituacaoReservaEntrega situacao);

    @EntityGraph(attributePaths = {"oferta", "oferta.doador", "oferta.itens", "receptor", "transportador"})
    List<ReservaEntrega> findByOfertaDoadorIdOrderByCriadoEmDesc(Long doadorId);

    boolean existsByReceptorId(Long receptorId);

    @EntityGraph(attributePaths = {"oferta", "oferta.doador", "oferta.itens", "receptor", "transportador"})
    Optional<ReservaEntrega> findWithDetalhesById(Long id);

    @EntityGraph(attributePaths = {"oferta", "oferta.doador", "oferta.itens", "receptor", "transportador"})
    Optional<ReservaEntrega> findFirstByOfertaIdAndSituacaoNot(Long ofertaId, SituacaoReservaEntrega situacao);

    @EntityGraph(attributePaths = {"oferta", "receptor", "transportador"})
    List<ReservaEntrega> findByOfertaIdInAndSituacaoNot(Collection<Long> ofertaIds, SituacaoReservaEntrega situacao);
}
