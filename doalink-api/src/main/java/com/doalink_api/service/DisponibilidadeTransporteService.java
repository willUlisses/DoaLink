package com.doalink_api.service;

import com.doalink_api.dtos.request.DisponibilidadeTransporteRequest;
import com.doalink_api.dtos.response.DisponibilidadeTransporteResponse;
import com.doalink_api.entities.DisponibilidadeTransporte;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.enums.SituacaoDisponibilidade;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.mapper.DisponibilidadeTransporteMapper;
import com.doalink_api.repository.DisponibilidadeTransporteRepository;
import com.doalink_api.repository.ReservaEntregaRepository;
import com.doalink_api.repository.TransportadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DisponibilidadeTransporteService {

    private static final List<SituacaoDisponibilidade> SITUACOES_ATIVAS = List.of(
            SituacaoDisponibilidade.DISPONIVEL,
            SituacaoDisponibilidade.RESERVADA
    );

    private final DisponibilidadeTransporteRepository disponibilidadeRepository;
    private final TransportadorRepository transportadorRepository;
    private final ReservaEntregaRepository reservaRepository;

    @Transactional
    public DisponibilidadeTransporteResponse criar(Long usuarioId, DisponibilidadeTransporteRequest req) {
        Transportador transportador = transportadorRepository.findById(usuarioId)
                .orElseThrow(() -> new ForbiddenException("Apenas transportadores podem cadastrar disponibilidades"));

        boolean jaPossuiAtivaNaData = disponibilidadeRepository.existsByTransportadorIdAndDataDisponivelAndSituacaoIn(
                usuarioId, req.dataDisponivel(), SITUACOES_ATIVAS);

        if (jaPossuiAtivaNaData) {
            throw new ConflictException("Já existe uma disponibilidade ativa para a data " + req.dataDisponivel());
        }

        DisponibilidadeTransporte disponibilidade = DisponibilidadeTransporteMapper.toEntity(req, transportador);
        return DisponibilidadeTransporteResponse.from(disponibilidadeRepository.save(disponibilidade));
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeTransporteResponse> listarMinhas(Long usuarioId) {
        return disponibilidadeRepository.findByTransportadorIdOrderByDataDisponivelAscCriadoEmDesc(usuarioId)
                .stream()
                .map(DisponibilidadeTransporteResponse::from)
                .toList();
    }

    @Transactional
    public void cancelar(Long usuarioId, Long disponibilidadeId) {
        DisponibilidadeTransporte disponibilidade = disponibilidadeRepository.findWithTransportadorById(disponibilidadeId)
                .orElseThrow(() -> new NotFoundException("Disponibilidade não encontrada: " + disponibilidadeId));

        if (!disponibilidade.getTransportador().getId().equals(usuarioId)) {
            throw new ForbiddenException("Você não tem permissão para cancelar esta disponibilidade");
        }

        if (disponibilidade.getSituacao() != SituacaoDisponibilidade.DISPONIVEL) {
            throw new ForbiddenException(
                    "Só é possível cancelar disponibilidades com situação DISPONIVEL. Situação atual: "
                            + disponibilidade.getSituacao());
        }

        disponibilidade.setSituacao(SituacaoDisponibilidade.CANCELADA);
    }

    @Transactional
    public void encerrar(Long usuarioId, Long disponibilidadeId) {
        DisponibilidadeTransporte d = disponibilidadeRepository.findWithTransportadorById(disponibilidadeId)
                .orElseThrow(() -> new NotFoundException("Disponibilidade não encontrada: " + disponibilidadeId));

        if (!d.getTransportador().getId().equals(usuarioId)) {
            throw new ForbiddenException("Você não tem permissão para encerrar esta disponibilidade");
        }

        if (d.getSituacao() != SituacaoDisponibilidade.RESERVADA) {
            throw new ConflictException("Só é possível encerrar disponibilidades RESERVADAS. Situação atual: "
                    + d.getSituacao());
        }

        if (reservaRepository.existsByDisponibilidadeIdAndSituacao(
                disponibilidadeId, SituacaoReservaEntrega.TRANSPORTE_VINCULADO)) {
            throw new ConflictException("Existem entregas vinculadas ainda não concluídas");
        }

        d.setSituacao(SituacaoDisponibilidade.CONCLUIDA);
    }
}

