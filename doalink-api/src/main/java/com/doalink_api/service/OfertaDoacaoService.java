package com.doalink_api.service;

import com.doalink_api.dtos.request.OfertaDoacaoRequest;
import com.doalink_api.dtos.response.ContatoResponse;
import com.doalink_api.dtos.response.OfertaDoacaoResponse;
import com.doalink_api.entities.DisponibilidadeTransporte;
import com.doalink_api.entities.OfertaDoacao;
import com.doalink_api.entities.ReservaEntrega;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.mapper.OfertaDoacaoMapper;
import com.doalink_api.repository.DoadorRepository;
import com.doalink_api.repository.OfertaDoacaoRepository;
import com.doalink_api.repository.ReservaEntregaRepository;
import com.doalink_api.repository.TransportadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Map<Long, ContatoResponse> receptorPorOferta = reservaRepository
                .findByOfertaIdInAndSituacaoNot(ofertaIds, SituacaoReservaEntrega.CANCELADA)
                .stream()
                .filter(r -> r.getOferta() != null && r.getReceptor() != null)
                .collect(Collectors.toMap(
                        r -> r.getOferta().getId(),
                        r -> ContatoResponse.from(r.getReceptor()),
                        (primeiro, segundo) -> primeiro
                ));

        return ofertas.stream()
                .map(oferta -> OfertaDoacaoResponse.from(oferta, true, receptorPorOferta.get(oferta.getId())))
                .toList();
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

        // Quando nenhum filtro é informado busca todas as DISPONIVEL
        if (categoria == null && cidade == null && estado == null) {
            return ofertaRepository.findBySituacao(SituacaoOferta.DISPONIVEL, pageable)
                    .map(o -> OfertaDoacaoResponse.from(o, false));
        }

        // Com filtros aplica predicados em memória sobre a página e reconstrói PageImpl.
        // Nota: total pode divergir do count real — queries JPA especializadas são a evolução futura.
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
                .map(ReservaEntrega::getDisponibilidade)
                .map(DisponibilidadeTransporte::getTransportador)
                .map(Transportador::getId)
                .map(tid -> tid.equals(usuarioId))
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

        return OfertaDoacaoResponse.from(oferta, podeVerDetalhesCompletos, receptorContato);
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
