package com.doalink_api.service;

import com.doalink_api.dtos.request.NecessidadeDoacaoRequest;
import com.doalink_api.dtos.response.NecessidadeDoacaoResponse;
import com.doalink_api.entities.NecessidadeDoacao;
import com.doalink_api.entities.enums.SituacaoNecessidade;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.mapper.NecessidadeDoacaoMapper;
import com.doalink_api.repository.NecessidadeDoacaoRepository;
import com.doalink_api.repository.ReceptorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NecessidadeDoacaoService {

    private final NecessidadeDoacaoRepository necessidadeRepository;
    private final ReceptorRepository receptorRepository;

    // -------------------------------------------------------------------------
    // Criar necessidade
    // -------------------------------------------------------------------------
    @Transactional
    public NecessidadeDoacaoResponse criar(Long usuarioId, NecessidadeDoacaoRequest req) {
        var receptor = receptorRepository.findById(usuarioId)
                .orElseThrow(() -> new ForbiddenException("Apenas receptores podem criar necessidades"));

        NecessidadeDoacao necessidade = NecessidadeDoacaoMapper.toEntity(req, receptor);
        return NecessidadeDoacaoResponse.from(necessidadeRepository.save(necessidade));
    }

    // -------------------------------------------------------------------------
    // Minhas necessidades (somente do receptor autenticado)
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<NecessidadeDoacaoResponse> listarMinhas(Long usuarioId) {
        return necessidadeRepository.findByReceptorIdOrderByCriadoEmDesc(usuarioId)
                .stream()
                .map(NecessidadeDoacaoResponse::from)
                .toList();
    }

    // -------------------------------------------------------------------------
    // Consulta autenticada — paginada, filtrada por situação
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public Page<NecessidadeDoacaoResponse> listarPorSituacao(SituacaoNecessidade situacao, Pageable pageable) {
        return necessidadeRepository.findBySituacao(situacao, pageable)
                .map(NecessidadeDoacaoResponse::from);
    }

    // -------------------------------------------------------------------------
    // Buscar por id
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public NecessidadeDoacaoResponse buscarPorId(Long id) {
        return necessidadeRepository.findWithItensById(id)
                .map(NecessidadeDoacaoResponse::from)
                .orElseThrow(() -> new NotFoundException("Necessidade não encontrada: " + id));
    }
}
