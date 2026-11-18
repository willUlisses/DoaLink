package com.doalink_api.service;

import com.doalink_api.dtos.request.ReservaEntregaRequest;
import com.doalink_api.dtos.request.VincularTransporteRequest;
import com.doalink_api.dtos.response.ReservaEntregaResponse;
import com.doalink_api.entities.*;
import com.doalink_api.entities.enums.SituacaoDisponibilidade;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.DisponibilidadeTransporteRepository;
import com.doalink_api.repository.OfertaDoacaoRepository;
import com.doalink_api.repository.ReceptorRepository;
import com.doalink_api.repository.ReservaEntregaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservaEntregaService {

    private final ReservaEntregaRepository reservaRepository;
    private final OfertaDoacaoRepository ofertaRepository;
    private final ReceptorRepository receptorRepository;
    private final DisponibilidadeTransporteRepository disponibilidadeRepository;

    @Transactional
    public ReservaEntregaResponse reservar(Long usuarioId, ReservaEntregaRequest req) {
        Receptor receptor = receptorRepository.findById(usuarioId)
                .orElseThrow(() -> new ForbiddenException("Apenas receptores podem reservar ofertas"));

        OfertaDoacao oferta = ofertaRepository.findWithItensById(req.ofertaId())
                .orElseThrow(() -> new NotFoundException("Oferta não encontrada: " + req.ofertaId()));

        if (oferta.getSituacao() != SituacaoOferta.DISPONIVEL) {
            throw new ConflictException("Oferta não está disponível para reserva. Situação atual: " + oferta.getSituacao());
        }

        oferta.setSituacao(SituacaoOferta.RESERVADA);

        ReservaEntrega reserva = new ReservaEntrega();
        reserva.setOferta(oferta);
        reserva.setReceptor(receptor);
        reserva.setPrecisaTransporte(req.precisaTransporte());
        reserva.setSituacao(req.precisaTransporte()
                ? SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE
                : SituacaoReservaEntrega.AGUARDANDO_RETIRADA);

        return ReservaEntregaResponse.from(reservaRepository.save(reserva), true);
    }

    @Transactional(readOnly = true)
    public List<ReservaEntregaResponse> listarAguardandoTransporte() {
        return reservaRepository.findBySituacaoOrderByCriadoEmDesc(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE)
                .stream()
                .map(r -> ReservaEntregaResponse.from(r, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaEntregaResponse> listarMinhasReceptor(Long usuarioId) {
        return reservaRepository.findByReceptorIdOrderByCriadoEmDesc(usuarioId)
                .stream()
                .map(r -> ReservaEntregaResponse.from(r, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaEntregaResponse> listarMinhasTransportador(Long usuarioId) {
        return reservaRepository.findByDisponibilidadeTransportadorIdOrderByCriadoEmDesc(usuarioId)
                .stream()
                .map(r -> ReservaEntregaResponse.from(r, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaEntregaResponse> listarMinhasDoador(Long usuarioId) {
        return reservaRepository.findByOfertaDoadorIdOrderByCriadoEmDesc(usuarioId)
                .stream()
                .map(r -> ReservaEntregaResponse.from(r, true))
                .toList();
    }

    @Transactional
    public ReservaEntregaResponse vincularTransporte(Long usuarioId, Long reservaId, VincularTransporteRequest req) {
        ReservaEntrega reserva = reservaRepository.findWithDetalhesById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservaId));

        if (reserva.getSituacao() != SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE) {
            throw new ConflictException("Reserva não está aguardando transporte. Situação atual: " + reserva.getSituacao());
        }

        DisponibilidadeTransporte disponibilidade = disponibilidadeRepository.findWithTransportadorById(req.disponibilidadeId())
                .orElseThrow(() -> new NotFoundException("Disponibilidade não encontrada: " + req.disponibilidadeId()));

        if (!disponibilidade.getTransportador().getId().equals(usuarioId)) {
            throw new ForbiddenException("Você não tem permissão para usar esta disponibilidade");
        }

        if (disponibilidade.getSituacao() != SituacaoDisponibilidade.DISPONIVEL
                && disponibilidade.getSituacao() != SituacaoDisponibilidade.RESERVADA) {
            throw new ConflictException("Disponibilidade não está ativa. Situação atual: " + disponibilidade.getSituacao());
        }

        if (disponibilidade.getDataDisponivel().isBefore(LocalDate.now())) {
            throw new ConflictException("Disponibilidade expirada em " + disponibilidade.getDataDisponivel());
        }

        List<ItemDoacao> itens = reserva.getOferta().getItens();
        boolean unidadeIncompativel = itens.stream()
                .anyMatch(item -> item.getTipoUnidade() != disponibilidade.getTipoUnidade());
        if (unidadeIncompativel) {
            throw new ConflictException("Unidade dos itens incompatível com a unidade da disponibilidade: "
                    + disponibilidade.getTipoUnidade());
        }

        double cargaTotal = itens.stream()
                .mapToDouble(ItemDoacao::getQuantidade)
                .sum();

        if (cargaTotal > disponibilidade.getCapacidadeRestante()) {
            throw new ConflictException("Capacidade restante insuficiente na disponibilidade");
        }

        disponibilidade.setCapacidadeRestante(disponibilidade.getCapacidadeRestante() - cargaTotal);
        if (disponibilidade.getSituacao() == SituacaoDisponibilidade.DISPONIVEL) {
            disponibilidade.setSituacao(SituacaoDisponibilidade.RESERVADA);
        }

        reserva.setDisponibilidade(disponibilidade);
        reserva.setSituacao(SituacaoReservaEntrega.TRANSPORTE_VINCULADO);

        return ReservaEntregaResponse.from(reserva, true);
    }

    @Transactional
    public void concluir(Long usuarioId, Long reservaId) {
        ReservaEntrega reserva = reservaRepository.findWithDetalhesById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservaId));

        if (reserva.getSituacao() == SituacaoReservaEntrega.TRANSPORTE_VINCULADO) {
            Long transportadorId = reserva.getDisponibilidade().getTransportador().getId();
            if (!transportadorId.equals(usuarioId)) {
                throw new ForbiddenException("Apenas o transportador vinculado pode concluir esta entrega");
            }
        } else if (reserva.getSituacao() == SituacaoReservaEntrega.AGUARDANDO_RETIRADA) {
            if (!reserva.getReceptor().getId().equals(usuarioId)) {
                throw new ForbiddenException("Apenas o receptor da reserva pode concluir esta retirada");
            }
        } else {
            throw new ConflictException("Reserva não pode ser concluída na situação atual: " + reserva.getSituacao());
        }

        reserva.setSituacao(SituacaoReservaEntrega.CONCLUIDA);
        reserva.getOferta().setSituacao(SituacaoOferta.CONCLUIDA);
    }

    @Transactional
    public void cancelar(Long usuarioId, Long reservaId) {
        ReservaEntrega reserva = reservaRepository.findWithDetalhesById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservaId));

        if (!reserva.getReceptor().getId().equals(usuarioId)) {
            throw new ForbiddenException("Apenas o receptor dono da reserva pode cancelá-la");
        }

        if (reserva.getSituacao() != SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE
                && reserva.getSituacao() != SituacaoReservaEntrega.AGUARDANDO_RETIRADA) {
            throw new ConflictException("Só é possível cancelar reserva antes do vínculo de transporte ou conclusão");
        }

        reserva.setSituacao(SituacaoReservaEntrega.CANCELADA);
        reserva.getOferta().setSituacao(SituacaoOferta.DISPONIVEL);
    }
}

