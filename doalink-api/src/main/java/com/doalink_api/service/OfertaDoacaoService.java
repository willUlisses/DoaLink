package com.doalink_api.service;

import com.doalink_api.dtos.request.OfertaDoacaoRequest;
import com.doalink_api.dtos.response.OfertaDoacaoResponse;
import com.doalink_api.entities.OfertaDoacao;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.mapper.OfertaDoacaoMapper;
import com.doalink_api.repository.DoadorRepository;
import com.doalink_api.repository.OfertaDoacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OfertaDoacaoService {

    private final OfertaDoacaoRepository ofertaRepository;
    private final DoadorRepository doadorRepository;

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
        return ofertaRepository.findByDoadorIdOrderByCriadoEmDesc(usuarioId)
                .stream()
                .map(OfertaDoacaoResponse::from)
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

        // Quando nenhum filtro é informado busca todas as DISPONIVEL
        if (categoria == null && cidade == null && estado == null) {
            return ofertaRepository.findBySituacao(SituacaoOferta.DISPONIVEL, pageable)
                    .map(OfertaDoacaoResponse::from);
        }

        // Com filtros aplica predicados em memória sobre a página e reconstrói PageImpl.
        // Nota: total pode divergir do count real — queries JPA especializadas são a evolução futura.
        Page<OfertaDoacaoResponse> pagina = ofertaRepository
                .findBySituacao(SituacaoOferta.DISPONIVEL, pageable)
                .map(OfertaDoacaoResponse::from);

        List<OfertaDoacaoResponse> filtrados = pagina.getContent().stream()
                .filter(r -> matchesFiltros(r, categoria, cidade, estado))
                .toList();

        return new PageImpl<>(filtrados, pageable, pagina.getTotalElements());
    }

    // -------------------------------------------------------------------------
    // Buscar por id
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public OfertaDoacaoResponse buscarPorId(Long id) {
        return ofertaRepository.findWithItensById(id)
                .map(OfertaDoacaoResponse::from)
                .orElseThrow(() -> new NotFoundException("Oferta não encontrada: " + id));
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
