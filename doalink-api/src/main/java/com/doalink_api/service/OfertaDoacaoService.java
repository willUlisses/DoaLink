package com.doalink_api.service;

import com.doalink_api.dtos.request.OfertaDoacaoRequest;
import com.doalink_api.dtos.response.ContatoResponse;
import com.doalink_api.dtos.response.OfertaDoacaoResponse;
import com.doalink_api.dtos.response.ReservaEntregaResponse;
import com.doalink_api.entities.CodigoTransporte;
import com.doalink_api.entities.OfertaDoacao;
import com.doalink_api.entities.ReservaEntrega;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import com.doalink_api.entities.enums.TipoCodigoTransporte;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.mapper.OfertaDoacaoMapper;
import com.doalink_api.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfertaDoacaoService {

    private final OfertaDoacaoRepository ofertaRepository;
    private final DoadorRepository doadorRepository;
    private final TransportadorRepository transportadorRepository;
    private final ReservaEntregaRepository reservaRepository;
    private final CodigoTransporteRepository codigoTransporteRepository;
    private final CodigoTransporteService codigoTransporteService;
    private final ReservaEntregaService reservaEntregaService;

    // -------------------------------------------------------------------------
    // Criar oferta
    // -------------------------------------------------------------------------
    @Transactional
    public OfertaDoacaoResponse criar(Long usuarioId, OfertaDoacaoRequest req) {
        var doador = doadorRepository.findById(usuarioId)
                .orElseThrow(() -> new ForbiddenException("Apenas doadores podem criar ofertas"));

        OfertaDoacao oferta = OfertaDoacaoMapper.toEntity(req, doador);
        return OfertaDoacaoResponse.from(ofertaRepository.save(oferta));
    }

    // -------------------------------------------------------------------------
    // Minhas ofertas (somente do doador autenticado)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<OfertaDoacaoResponse> listarMinhas(Long usuarioId) {
        List<OfertaDoacao> ofertas = ofertaRepository.findByDoadorIdOrderByCriadoEmDesc(usuarioId);
        if (ofertas.isEmpty()) {
            return List.of();
        }

        List<Long> ofertaIds = ofertas.stream().map(OfertaDoacao::getId).toList();
        Map<Long, ReservaEntrega> reservaPorOferta = reservaRepository
                .findByOfertaIdInAndSituacaoNot(ofertaIds, SituacaoReservaEntrega.CANCELADA)
                .stream()
                .filter(r -> r.getOferta() != null)
                .collect(Collectors.toMap(
                        r -> r.getOferta().getId(),
                        r -> r,
                        (primeiro, segundo) -> primeiro
                ));

        List<Long> reservasAguardandoRetiradaIds = reservaPorOferta.values().stream()
                .filter(r -> r.getSituacao() == SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA)
                .map(ReservaEntrega::getId)
                .toList();

        Map<Long, CodigoTransporte> codigosRetiradaPorReserva = reservasAguardandoRetiradaIds.isEmpty()
                ? Map.of()
                : codigoTransporteRepository
                        .findByReservaIdInAndTipoAndInvalidadoFalseAndUtilizadoFalse(
                                reservasAguardandoRetiradaIds, TipoCodigoTransporte.RETIRADA)
                        .stream()
                        .collect(Collectors.toMap(
                                c -> c.getReserva().getId(),
                                c -> c,
                                (primeiro, segundo) -> primeiro
                        ));

        LocalDateTime agora = LocalDateTime.now();

        return ofertas.stream().map(oferta -> {
            ReservaEntrega reservaAtiva = reservaPorOferta.get(oferta.getId());
            ContatoResponse receptorContato = (reservaAtiva != null && reservaAtiva.getReceptor() != null)
                    ? ContatoResponse.from(reservaAtiva.getReceptor())
                    : null;
            String codigoRetirada = null;
            if (reservaAtiva != null
                    && reservaAtiva.getSituacao() == SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA) {
                CodigoTransporte cod = codigosRetiradaPorReserva.get(reservaAtiva.getId());
                codigoRetirada = codigoTransporteService.reconstruirCodigoAtivo(cod, agora);
            }
            return OfertaDoacaoResponse.from(oferta, true, receptorContato, reservaAtiva, codigoRetirada);
        }).toList();
    }

    // -------------------------------------------------------------------------
    // Consulta pública — paginada, com filtros opcionais de categoria e UF/cidade
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<OfertaDoacaoResponse> listarDisponiveis(
            Pageable pageable,
            TipoItemDoacao categoria,
            String cidade,
            String estado) {
        return listarDisponiveis(pageable, categoria, cidade, estado, null);
    }

    @Transactional(readOnly = true)
    public Page<OfertaDoacaoResponse> listarDisponiveis(
            Pageable pageable,
            TipoItemDoacao categoria,
            String cidade,
            String estado,
            Long usuarioId) {

        if (usuarioId != null && doadorRepository.existsById(usuarioId)) {
            throw new ForbiddenException("Doadores só podem visualizar suas próprias ofertas");
        }

        if (categoria == null && cidade == null && estado == null) {
            return ofertaRepository.findBySituacao(SituacaoOferta.DISPONIVEL, pageable)
                    .map(o -> OfertaDoacaoResponse.from(o, false));
        }

        Page<OfertaDoacaoResponse> pagina = ofertaRepository
                .findBySituacao(SituacaoOferta.DISPONIVEL, pageable)
                .map(o -> OfertaDoacaoResponse.from(o, false));

        List<OfertaDoacaoResponse> filtrados = pagina.getContent().stream()
                .filter(r -> matchesFiltros(r, categoria, cidade, estado))
                .toList();

        return new PageImpl<>(filtrados, pageable, pagina.getTotalElements());
    }

    // -------------------------------------------------------------------------
    // Buscar por id
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public OfertaDoacaoResponse buscarPorId(Long id, Long usuarioId) {
        OfertaDoacao oferta = ofertaRepository.findWithItensById(id)
                .orElseThrow(() -> new NotFoundException("Oferta não encontrada: " + id));

        boolean isDono = oferta.getDoador() != null && oferta.getDoador().getId().equals(usuarioId);

        Optional<ReservaEntrega> reservaAtiva = reservaRepository
                .findFirstByOfertaIdAndSituacaoNot(id, SituacaoReservaEntrega.CANCELADA);

        boolean isReceptorDaReserva = reservaAtiva
                .map(r -> r.getReceptor() != null && r.getReceptor().getId().equals(usuarioId))
                .orElse(false);

        boolean isTransportadorVinculado = reservaAtiva
                .map(ReservaEntrega::getTransportador)
                .map(t -> t.getId().equals(usuarioId))
                .orElse(false);

        if (!isDono && doadorRepository.existsById(usuarioId)) {
            throw new ForbiddenException("Doadores só podem visualizar suas próprias ofertas");
        }

        boolean isTransportador = !isTransportadorVinculado && transportadorRepository.existsById(usuarioId);
        if (isTransportador && oferta.getSituacao() != SituacaoOferta.RESERVADA) {
            throw new ForbiddenException(
                    "Transportadores só podem visualizar ofertas RESERVADAS ou vinculadas a eles");
        }

        if (!isDono
                && !isReceptorDaReserva
                && !isTransportadorVinculado
                && !isTransportador
                && oferta.getSituacao() != SituacaoOferta.DISPONIVEL) {
            throw new ForbiddenException(
                    "Esta oferta não está disponível para visualização por outra organização");
        }

        boolean podeVerDetalhesCompletos = isDono || isReceptorDaReserva || isTransportadorVinculado;
        ContatoResponse receptorContato = podeVerDetalhesCompletos
                ? reservaAtiva.map(ReservaEntrega::getReceptor).map(ContatoResponse::from).orElse(null)
                : null;

        String codigoRetirada = null;
        if (isDono && reservaAtiva.isPresent()
                && reservaAtiva.get().getSituacao() == SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA) {
            CodigoTransporte cod = codigoTransporteRepository
                    .findFirstByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(
                            reservaAtiva.get().getId(), TipoCodigoTransporte.RETIRADA)
                    .orElse(null);
            codigoRetirada = codigoTransporteService.reconstruirCodigoAtivo(cod, LocalDateTime.now());
        }

        return OfertaDoacaoResponse.from(
                oferta,
                podeVerDetalhesCompletos,
                receptorContato,
                podeVerDetalhesCompletos ? reservaAtiva.orElse(null) : null,
                codigoRetirada
        );
    }

    // -------------------------------------------------------------------------
    // Autorizar início do transporte (atalho pela oferta)
    // -------------------------------------------------------------------------
    @Transactional
    public ReservaEntregaResponse autorizarInicioTransporte(Long usuarioId, Long ofertaId) {
        return reservaEntregaService.autorizarInicioTransportePorOferta(usuarioId, ofertaId);
    }

    // -------------------------------------------------------------------------
    // Cancelar — apenas o dono, apenas se DISPONIVEL
    // -------------------------------------------------------------------------
    @Transactional
    public void cancelar(Long usuarioId, Long ofertaId) {
        OfertaDoacao oferta = ofertaRepository.findWithItensById(ofertaId)
                .orElseThrow(() -> new NotFoundException("Oferta não encontrada: " + ofertaId));

        if (!oferta.getDoador().getId().equals(usuarioId)) {
            throw new ForbiddenException("Você não tem permissão para cancelar esta oferta");
        }

        if (oferta.getSituacao() != SituacaoOferta.DISPONIVEL) {
            throw new ForbiddenException(
                    "Só é possível cancelar ofertas com situação DISPONIVEL. Situação atual: "
                            + oferta.getSituacao());
        }

        oferta.setSituacao(SituacaoOferta.CANCELADA);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private boolean matchesFiltros(OfertaDoacaoResponse r,
                                   TipoItemDoacao categoria,
                                   String cidade,
                                   String estado) {
        if (categoria != null && r.itens().stream()
                .noneMatch(i -> i.tipoItemDoacao() == categoria)) {
            return false;
        }
        if (cidade != null && (r.localRetirada() == null
                || !r.localRetirada().cidade().equalsIgnoreCase(cidade))) {
            return false;
        }
        if (estado != null && (r.localRetirada() == null
                || !r.localRetirada().estado().equalsIgnoreCase(estado))) {
            return false;
        }
        return true;
    }
}
