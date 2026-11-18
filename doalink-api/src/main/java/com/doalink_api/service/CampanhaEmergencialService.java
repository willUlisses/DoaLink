package com.doalink_api.service;

import com.doalink_api.dtos.request.CampanhaEmergencialRequest;
import com.doalink_api.dtos.response.CampanhaEmergencialResponse;
import com.doalink_api.entities.CampanhaEmergencial;
import com.doalink_api.entities.enums.SituacaoCampanha;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.mapper.CampanhaEmergencialMapper;
import com.doalink_api.repository.CampanhaEmergencialRepository;
import com.doalink_api.repository.ReceptorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CampanhaEmergencialService {

    private final CampanhaEmergencialRepository campanhaRepository;
    private final ReceptorRepository receptorRepository;

    @Transactional
    public CampanhaEmergencialResponse criar(Long usuarioId, CampanhaEmergencialRequest req) {
        var receptor = receptorRepository.findById(usuarioId)
                .orElseThrow(() -> new ForbiddenException("Apenas receptores podem criar campanhas"));

        CampanhaEmergencial campanha = CampanhaEmergencialMapper.toEntity(req, receptor);
        return CampanhaEmergencialResponse.from(campanhaRepository.save(campanha));
    }

    @Transactional(readOnly = true)
    public List<CampanhaEmergencialResponse> listarMinhas(Long usuarioId) {
        return campanhaRepository.findByReceptorIdOrderByCriadoEmDesc(usuarioId)
                .stream()
                .map(CampanhaEmergencialResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<CampanhaEmergencialResponse> listarPorSituacao(SituacaoCampanha situacao, Pageable pageable) {
        return campanhaRepository.findBySituacao(situacao, pageable)
                .map(CampanhaEmergencialResponse::from);
    }

    @Transactional(readOnly = true)
    public CampanhaEmergencialResponse buscarPorId(Long id) {
        return campanhaRepository.findWithItensById(id)
                .map(CampanhaEmergencialResponse::from)
                .orElseThrow(() -> new NotFoundException("Campanha emergencial não encontrada: " + id));
    }
}
