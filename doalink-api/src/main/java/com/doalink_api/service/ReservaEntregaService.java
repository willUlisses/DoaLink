package com.doalink_api.service;

import com.doalink_api.dtos.request.ConfirmarCodigoTransporteRequest;
import com.doalink_api.dtos.request.ReservaEntregaRequest;
import com.doalink_api.dtos.response.EventoTransporteResponse;
import com.doalink_api.dtos.response.ReservaEntregaResponse;
import com.doalink_api.entities.*;
import com.doalink_api.entities.enums.*;
import com.doalink_api.exception.CodigoInvalidoException;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaEntregaService {

    private final ReservaEntregaRepository reservaRepository;
    private final OfertaDoacaoRepository ofertaRepository;
    private final ReceptorRepository receptorRepository;
    private final TransportadorRepository transportadorRepository;
    private final CodigoTransporteRepository codigoTransporteRepository;
    private final EventoTransporteRepository eventoTransporteRepository;
    private final OcorrenciaTransporteRepository ocorrenciaTransporteRepository;
    private final CodigoTransporteService codigoTransporteService;

    // -------------------------------------------------------------------------
    // Reserva pelo Receptor
    // -------------------------------------------------------------------------

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

        boolean ofertaPossuiEndereco = possuiEndereco(oferta.getLocalRetirada());
        boolean precisaTransporte = ofertaPossuiEndereco && Boolean.TRUE.equals(req.precisaTransporte());

        ReservaEntrega reserva = new ReservaEntrega();
        reserva.setOferta(oferta);
        reserva.setReceptor(receptor);
        reserva.setPrecisaTransporte(precisaTransporte);
        reserva.setSituacao(precisaTransporte
                ? SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE
                : SituacaoReservaEntrega.AGUARDANDO_RETIRADA);

        return ReservaEntregaResponse.from(reservaRepository.save(reserva), true);
    }

    // -------------------------------------------------------------------------
    // Listagens e consulta detalhada
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ReservaEntregaResponse> listarAguardandoTransporte() {
        return reservaRepository.findBySituacaoOrderByCriadoEmDesc(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE)
                .stream()
                .map(r -> ReservaEntregaResponse.from(r, false))
                .toList();
    }

    @Transactional
    public List<ReservaEntregaResponse> listarMinhasReceptor(Long usuarioId) {
        processarExpiracoesPendentes();
        List<ReservaEntrega> reservas = reservaRepository.findByReceptorIdOrderByCriadoEmDesc(usuarioId);
        Map<Long, CodigoTransporte> codigosEntregaPorReserva = carregarCodigosAtivosPorReserva(
                reservas, SituacaoReservaEntrega.EM_TRANSPORTE, TipoCodigoTransporte.ENTREGA);
        LocalDateTime agora = LocalDateTime.now();

        return reservas.stream().map(r -> {
            CodigoTransporte codigo = codigosEntregaPorReserva.get(r.getId());
            String codigoEntrega = codigoTransporteService.reconstruirCodigoAtivo(codigo, agora);
            LocalDateTime expiraEm = codigo != null ? codigo.getExpiraEm() : null;
            return ReservaEntregaResponse.from(r, true, null, codigoEntrega, expiraEm);
        }).toList();
    }

    @Transactional
    public List<ReservaEntregaResponse> listarMinhasTransportador(Long usuarioId) {
        processarExpiracoesPendentes();
        List<ReservaEntrega> reservas = reservaRepository.findByTransportadorIdOrderByCriadoEmDesc(usuarioId);
        Map<Long, CodigoTransporte> codigosEntregaPorReserva = carregarCodigosAtivosPorReserva(
                reservas, SituacaoReservaEntrega.EM_TRANSPORTE, TipoCodigoTransporte.ENTREGA);

        return reservas.stream().map(r -> {
            CodigoTransporte codigo = codigosEntregaPorReserva.get(r.getId());
            LocalDateTime expiraEm = codigo != null ? codigo.getExpiraEm() : null;
            return ReservaEntregaResponse.from(r, true, null, null, expiraEm);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaEntregaResponse> listarHistoricoTransportador(Long usuarioId) {
        return reservaRepository.findByTransportadorIdAndSituacaoOrderByCriadoEmDesc(
                        usuarioId, SituacaoReservaEntrega.CONCLUIDA)
                .stream()
                .map(r -> ReservaEntregaResponse.from(r, true))
                .toList();
    }

    @Transactional
    public List<ReservaEntregaResponse> listarMinhasDoador(Long usuarioId) {
        processarExpiracoesPendentes();
        List<ReservaEntrega> reservas = reservaRepository.findByOfertaDoadorIdOrderByCriadoEmDesc(usuarioId);
        Map<Long, CodigoTransporte> codigosRetiradaPorReserva = carregarCodigosAtivosPorReserva(
                reservas, SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA, TipoCodigoTransporte.RETIRADA);
        LocalDateTime agora = LocalDateTime.now();

        return reservas.stream().map(r -> {
            CodigoTransporte codigo = codigosRetiradaPorReserva.get(r.getId());
            String codigoRetirada = codigoTransporteService.reconstruirCodigoAtivo(codigo, agora);
            return ReservaEntregaResponse.from(r, true, codigoRetirada, null, null);
        }).toList();
    }

    @Transactional
    public ReservaEntregaResponse buscarPorId(Long usuarioId, Long reservaId) {
        processarExpiracoesPendentes();
        ReservaEntrega reserva = reservaRepository.findWithDetalhesById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservaId));

        boolean isDoador = reserva.getOferta() != null
                && reserva.getOferta().getDoador() != null
                && reserva.getOferta().getDoador().getId().equals(usuarioId);
        boolean isReceptor = reserva.getReceptor() != null
                && reserva.getReceptor().getId().equals(usuarioId);
        boolean isTransportadorVinculado = reserva.getTransportador() != null
                && reserva.getTransportador().getId().equals(usuarioId);

        if (!isDoador && !isReceptor && !isTransportadorVinculado) {
            if (reserva.getSituacao() == SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE
                    && transportadorRepository.existsById(usuarioId)) {
                return ReservaEntregaResponse.from(reserva, false);
            }
            throw new ForbiddenException("Você não tem permissão para visualizar os detalhes desta operação");
        }

        LocalDateTime agora = LocalDateTime.now();
        String codigoRetirada = null;
        String codigoEntrega = null;
        LocalDateTime codigoEntregaExpiraEm = null;

        if (isDoador && reserva.getSituacao() == SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA) {
            CodigoTransporte codigo = codigoTransporteRepository
                    .findFirstByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(
                            reservaId, TipoCodigoTransporte.RETIRADA)
                    .orElse(null);
            codigoRetirada = codigoTransporteService.reconstruirCodigoAtivo(codigo, agora);
        }

        if (reserva.getSituacao() == SituacaoReservaEntrega.EM_TRANSPORTE) {
            CodigoTransporte codigo = codigoTransporteRepository
                    .findFirstByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(
                            reservaId, TipoCodigoTransporte.ENTREGA)
                    .orElse(null);
            if (codigo != null) {
                codigoEntregaExpiraEm = codigo.getExpiraEm();
                if (isReceptor) {
                    codigoEntrega = codigoTransporteService.reconstruirCodigoAtivo(codigo, agora);
                }
            }
        }

        List<EventoTransporteResponse> eventos = eventoTransporteRepository
                .findByReservaIdOrderByCriadoEmAsc(reservaId)
                .stream()
                .map(EventoTransporteResponse::from)
                .toList();

        return ReservaEntregaResponse.from(reserva, true, codigoRetirada, codigoEntrega, codigoEntregaExpiraEm, eventos);
    }

    // -------------------------------------------------------------------------
    // Fluxo de transporte: Aceitar -> Autorizar -> Retirada -> Entrega
    // -------------------------------------------------------------------------

    @Transactional
    public ReservaEntregaResponse aceitarTransporte(Long usuarioId, Long reservaId) {
        processarExpiracoesPendentes();

        Transportador transportador = transportadorRepository.findById(usuarioId)
                .orElseThrow(() -> new ForbiddenException("Apenas transportadores podem aceitar transportes"));

        if (transportador.isSuspenso()) {
            throw new ForbiddenException(
                    "Sua conta está suspensa e sob investigação. Não é possível aceitar novos transportes.");
        }

        ReservaEntrega reserva = reservaRepository.findWithDetalhesById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservaId));

        if (!reserva.isPrecisaTransporte()) {
            throw new ConflictException("Esta reserva é de retirada direta e não necessita de transportador");
        }

        if (reserva.getSituacao() != SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE || reserva.getTransportador() != null) {
            throw new ConflictException(
                    "Reserva não está aguardando transporte. Situação atual: " + reserva.getSituacao());
        }

        LocalDateTime agora = LocalDateTime.now();
        reserva.setTransportador(transportador);
        reserva.setAceitoEm(agora);
        reserva.setSituacao(SituacaoReservaEntrega.AGUARDANDO_AUTORIZACAO_DOADOR);

        ReservaEntrega salva = reservaRepository.save(reserva);
        registrarEvento(salva, transportador, TipoEventoTransporte.TRANSPORTE_ACEITO,
                "Transportador #" + transportador.getId() + " assumiu o transporte da reserva.");

        return ReservaEntregaResponse.from(salva, true);
    }

    @Transactional
    public ReservaEntregaResponse autorizarInicioTransporte(Long usuarioId, Long reservaId) {
        ReservaEntrega reserva = reservaRepository.findWithDetalhesById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservaId));
        return executarAutorizacaoInicio(usuarioId, reserva);
    }

    @Transactional
    public ReservaEntregaResponse autorizarInicioTransportePorOferta(Long usuarioId, Long ofertaId) {
        ReservaEntrega reserva = reservaRepository
                .findFirstByOfertaIdAndSituacaoNot(ofertaId, SituacaoReservaEntrega.CANCELADA)
                .orElseThrow(() -> new NotFoundException("Nenhuma reserva ativa encontrada para a oferta: " + ofertaId));
        return executarAutorizacaoInicio(usuarioId, reserva);
    }

    private ReservaEntregaResponse executarAutorizacaoInicio(Long usuarioId, ReservaEntrega reserva) {
        Doador doador = reserva.getOferta() != null ? reserva.getOferta().getDoador() : null;
        if (doador == null || !doador.getId().equals(usuarioId)) {
            throw new ForbiddenException("Apenas o doador responsável pela oferta pode autorizar o início do transporte");
        }

        if (reserva.getTransportador() == null
                || reserva.getSituacao() != SituacaoReservaEntrega.AGUARDANDO_AUTORIZACAO_DOADOR) {
            throw new ConflictException(
                    "Esta operação não está aguardando autorização do doador. Situação atual: " + reserva.getSituacao());
        }

        invalidarCodigosAtivos(reserva.getId(), TipoCodigoTransporte.RETIRADA);

        LocalDateTime agora = LocalDateTime.now();
        reserva.setAutorizadoEm(agora);
        reserva.setSituacao(SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA);
        ReservaEntrega salva = reservaRepository.save(reserva);

        CodigoTransporte codigoRetiradaEntity = codigoTransporteService.criarCodigo(
                salva, salva.getTransportador(), TipoCodigoTransporte.RETIRADA, null);
        codigoTransporteRepository.save(codigoRetiradaEntity);

        String codigoRetirada = codigoTransporteService.reconstruirCodigoAtivo(codigoRetiradaEntity, agora);

        registrarEvento(salva, doador, TipoEventoTransporte.TRANSPORTE_AUTORIZADO_DOADOR,
                "Doador autorizou o início do transporte.");
        registrarEvento(salva, doador, TipoEventoTransporte.CODIGO_RETIRADA_GERADO,
                "Código de confirmação de retirada gerado para o doador.");

        return ReservaEntregaResponse.from(salva, true, codigoRetirada, null, null);
    }

    @Transactional(noRollbackFor = {CodigoInvalidoException.class, ForbiddenException.class})
    public ReservaEntregaResponse confirmarRetirada(Long usuarioId, Long reservaId, ConfirmarCodigoTransporteRequest req) {
        ReservaEntrega reserva = reservaRepository.findWithDetalhesById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservaId));

        if (reserva.getTransportador() == null || !reserva.getTransportador().getId().equals(usuarioId)) {
            throw new ForbiddenException("Apenas o transportador vinculado pode confirmar a retirada desta operação");
        }

        Transportador transportador = reserva.getTransportador();
        if (transportador.isSuspenso()) {
            throw new ForbiddenException(
                    "Sua conta está suspensa e sob investigação. Operação bloqueada.");
        }

        if (reserva.getSituacao() != SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA) {
            throw new ConflictException(
                    "A operação não está aguardando confirmação de retirada. Situação atual: " + reserva.getSituacao());
        }

        CodigoTransporte codigoEntity = codigoTransporteRepository
                .findFirstByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(
                        reservaId, TipoCodigoTransporte.RETIRADA)
                .orElseThrow(() -> new CodigoInvalidoException(
                        "Nenhum código de retirada ativo disponível. Solicite verificação ao suporte."));

        if (codigoEntity.isBloqueado() || codigoEntity.getTentativas() >= codigoTransporteService.getMaxTentativas()) {
            codigoEntity.bloquear();
            codigoTransporteRepository.save(codigoEntity);
            throw new CodigoInvalidoException(
                    "Limite máximo de tentativas excedido para o código de retirada. Operação bloqueada por segurança.");
        }

        if (!codigoTransporteService.validarCodigo(codigoEntity, req.codigo())) {
            int novasTentativas = codigoEntity.getTentativas() + 1;
            codigoEntity.setTentativas(novasTentativas);
            registrarEvento(reserva, transportador, TipoEventoTransporte.TENTATIVA_CODIGO_RETIRADA_INVALIDA,
                    "Tentativa inválida #" + novasTentativas + " de código de retirada.");

            if (novasTentativas >= codigoTransporteService.getMaxTentativas()) {
                codigoEntity.bloquear();
                registrarEvento(reserva, transportador, TipoEventoTransporte.CODIGO_RETIRADA_BLOQUEADO,
                        "Código de retirada bloqueado após atingir o limite de " + novasTentativas + " tentativas.");
            }
            codigoTransporteRepository.save(codigoEntity);
            throw new CodigoInvalidoException("Código de retirada inválido");
        }

        LocalDateTime agora = LocalDateTime.now();
        codigoEntity.marcarUtilizado(agora);
        codigoTransporteRepository.save(codigoEntity);

        invalidarCodigosAtivos(reserva.getId(), TipoCodigoTransporte.ENTREGA);

        reserva.setRetiradaConfirmadaEm(agora);
        reserva.setSituacao(SituacaoReservaEntrega.EM_TRANSPORTE);
        ReservaEntrega salva = reservaRepository.save(reserva);

        LocalDateTime expiraEntregaEm = agora.plusHours(codigoTransporteService.getValidadeEntregaHoras());
        CodigoTransporte codigoEntregaEntity = codigoTransporteService.criarCodigo(
                salva, transportador, TipoCodigoTransporte.ENTREGA, expiraEntregaEm);
        codigoTransporteRepository.save(codigoEntregaEntity);

        registrarEvento(salva, transportador, TipoEventoTransporte.RETIRADA_CONFIRMADA,
                "Retirada dos itens confirmada pelo transportador com código válido.");
        registrarEvento(salva, transportador, TipoEventoTransporte.CODIGO_ENTREGA_GERADO,
                "Código de entrega gerado para o receptor com validade de 24 horas (expira em " + expiraEntregaEm + ").");

        return ReservaEntregaResponse.from(salva, true, null, null, expiraEntregaEm);
    }

    @Transactional(noRollbackFor = {CodigoInvalidoException.class, ForbiddenException.class})
    public ReservaEntregaResponse confirmarEntrega(Long usuarioId, Long reservaId, ConfirmarCodigoTransporteRequest req) {
        ReservaEntrega reserva = reservaRepository.findWithDetalhesById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservaId));

        if (reserva.getTransportador() == null || !reserva.getTransportador().getId().equals(usuarioId)) {
            throw new ForbiddenException("Apenas o transportador vinculado pode confirmar a entrega desta operação");
        }

        Transportador transportador = reserva.getTransportador();

        if (reserva.getSituacao() == SituacaoReservaEntrega.EM_INVESTIGACAO) {
            registrarEvento(reserva, transportador, TipoEventoTransporte.TENTATIVA_USO_CODIGO_EXPIRADO,
                    "Tentativa de confirmar entrega em operação já bloqueada sob investigação.");
            throw new ForbiddenException(
                    "Operação bloqueada por expiração do prazo de entrega. Sua conta está suspensa e sob investigação.");
        }

        if (reserva.getSituacao() != SituacaoReservaEntrega.EM_TRANSPORTE || reserva.getRetiradaConfirmadaEm() == null) {
            throw new ConflictException(
                    "Não é possível confirmar a entrega sem a confirmação prévia da retirada. Situação atual: "
                            + reserva.getSituacao());
        }

        CodigoTransporte codigoEntity = codigoTransporteRepository
                .findFirstByReservaIdAndTipoOrderByCriadoEmDesc(reservaId, TipoCodigoTransporte.ENTREGA)
                .orElseThrow(() -> new ConflictException("Código de entrega não localizado para esta operação"));

        LocalDateTime agora = LocalDateTime.now();
        if (codigoEntity.estaExpirado(agora)) {
            registrarEvento(reserva, transportador, TipoEventoTransporte.TENTATIVA_USO_CODIGO_EXPIRADO,
                    "Transportador tentou utilizar o código de entrega após o prazo de 24 horas.");
            aplicarSuspensaoPorExpiracao(reserva, codigoEntity, agora);
            throw new ForbiddenException(
                    "O prazo de 24 horas do código de entrega expirou. A operação está bloqueada e sua conta está suspensa sob investigação.");
        }

        if (transportador.isSuspenso()) {
            throw new ForbiddenException(
                    "Sua conta está suspensa e sob investigação. Não é possível concluir esta operação.");
        }

        if (codigoEntity.isUtilizado() || codigoEntity.isInvalidado()) {
            throw new CodigoInvalidoException("Este código de entrega já foi utilizado ou invalidado");
        }

        if (codigoEntity.isBloqueado() || codigoEntity.getTentativas() >= codigoTransporteService.getMaxTentativas()) {
            codigoEntity.bloquear();
            codigoTransporteRepository.save(codigoEntity);
            throw new CodigoInvalidoException(
                    "Limite máximo de tentativas excedido para o código de entrega. Operação bloqueada por segurança.");
        }

        if (!codigoTransporteService.validarCodigo(codigoEntity, req.codigo())) {
            int novasTentativas = codigoEntity.getTentativas() + 1;
            codigoEntity.setTentativas(novasTentativas);
            registrarEvento(reserva, transportador, TipoEventoTransporte.TENTATIVA_CODIGO_ENTREGA_INVALIDA,
                    "Tentativa inválida #" + novasTentativas + " de código de entrega.");

            if (novasTentativas >= codigoTransporteService.getMaxTentativas()) {
                codigoEntity.bloquear();
                registrarEvento(reserva, transportador, TipoEventoTransporte.CODIGO_ENTREGA_BLOQUEADO,
                        "Código de entrega bloqueado após atingir o limite de " + novasTentativas + " tentativas.");
            }
            codigoTransporteRepository.save(codigoEntity);
            throw new CodigoInvalidoException("Código de entrega inválido");
        }

        codigoEntity.marcarUtilizado(agora);
        codigoTransporteRepository.save(codigoEntity);

        reserva.setEntregaConfirmadaEm(agora);
        reserva.setSituacao(SituacaoReservaEntrega.CONCLUIDA);
        reserva.getOferta().setSituacao(SituacaoOferta.CONCLUIDA);
        ReservaEntrega salva = reservaRepository.save(reserva);

        registrarEvento(salva, transportador, TipoEventoTransporte.ENTREGA_CONFIRMADA,
                "Entrega confirmada com sucesso mediante código de segurança do receptor.");

        return ReservaEntregaResponse.from(salva, true);
    }

    // -------------------------------------------------------------------------
    // Retirada direta sem transportador e cancelamento
    // -------------------------------------------------------------------------

    @Transactional
    public void concluir(Long usuarioId, Long reservaId) {
        ReservaEntrega reserva = reservaRepository.findWithDetalhesById(reservaId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada: " + reservaId));

        if (reserva.getSituacao() != SituacaoReservaEntrega.AGUARDANDO_RETIRADA) {
            throw new ConflictException(
                    "Operações com transporte devem ser concluídas exclusivamente com os códigos de retirada e entrega. Situação atual: "
                            + reserva.getSituacao());
        }

        if (!reserva.getReceptor().getId().equals(usuarioId)) {
            throw new ForbiddenException("Apenas o receptor da reserva pode concluir esta retirada");
        }

        reserva.setEntregaConfirmadaEm(LocalDateTime.now());
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
            throw new ConflictException("Só é possível cancelar reserva antes da aceitação do transporte ou conclusão");
        }

        reserva.setSituacao(SituacaoReservaEntrega.CANCELADA);
        reserva.getOferta().setSituacao(SituacaoOferta.DISPONIVEL);
    }

    // -------------------------------------------------------------------------
    // Expiração de 24h e suspensão do transportador
    // -------------------------------------------------------------------------

    @Scheduled(fixedDelayString = "${app.transporte.verificacao-expiracao-ms:60000}")
    @Transactional
    public void processarExpiracoesPendentes() {
        LocalDateTime agora = LocalDateTime.now();
        List<CodigoTransporte> expirados = codigoTransporteRepository
                .findByTipoAndUtilizadoFalseAndInvalidadoFalseAndExpiraEmBefore(TipoCodigoTransporte.ENTREGA, agora);

        for (CodigoTransporte codigo : expirados) {
            aplicarSuspensaoPorExpiracao(codigo.getReserva(), codigo, agora);
        }
    }

    private void aplicarSuspensaoPorExpiracao(ReservaEntrega reserva, CodigoTransporte codigo, LocalDateTime agora) {
        codigo.invalidar();
        codigoTransporteRepository.save(codigo);

        if (reserva.getSituacao() == SituacaoReservaEntrega.EM_TRANSPORTE) {
            reserva.setSituacao(SituacaoReservaEntrega.EM_INVESTIGACAO);
            reservaRepository.save(reserva);
        }

        Transportador transportador = reserva.getTransportador();
        if (transportador != null && !transportador.isSuspenso()) {
            transportador.setSituacao(SituacaoTransportador.SUSPENSO);
            transportador.setSuspensoEm(agora);
            transportador.setMotivoSuspensao(
                    "Expiração do prazo de 24 horas sem confirmação de entrega na Reserva #" + reserva.getId());
            transportadorRepository.save(transportador);
        }

        if (!ocorrenciaTransporteRepository.existsByReservaId(reserva.getId()) && transportador != null) {
            OcorrenciaTransporte ocorrencia = new OcorrenciaTransporte();
            ocorrencia.setReserva(reserva);
            ocorrencia.setOferta(reserva.getOferta());
            ocorrencia.setTransportador(transportador);
            ocorrencia.setDoador(reserva.getOferta().getDoador());
            ocorrencia.setReceptor(reserva.getReceptor());
            ocorrencia.setMotivo("EXPIRACAO_CODIGO_ENTREGA_24H");
            ocorrencia.setDescricao(String.format(
                    "Transportador #%d (%s) confirmou a retirada em %s, mas não confirmou a entrega até o prazo limite (%s). Conta suspensa automaticamente para investigação.",
                    transportador.getId(),
                    transportador.getNome(),
                    reserva.getRetiradaConfirmadaEm(),
                    codigo.getExpiraEm()
            ));
            ocorrencia.setSituacao(SituacaoOcorrenciaTransporte.PENDENTE_INVESTIGACAO);
            ocorrencia.setAceitoEm(reserva.getAceitoEm());
            ocorrencia.setAutorizadoEm(reserva.getAutorizadoEm());
            ocorrencia.setRetiradaConfirmadaEm(reserva.getRetiradaConfirmadaEm());
            ocorrencia.setPrazoEntregaExpiradoEm(codigo.getExpiraEm() != null ? codigo.getExpiraEm() : agora);
            ocorrencia.setTentativasValidacaoEntrega(codigo.getTentativas());
            ocorrenciaTransporteRepository.save(ocorrencia);

            registrarEvento(reserva, transportador, TipoEventoTransporte.CODIGO_ENTREGA_EXPIRADO,
                    "Prazo de 24 horas do código de entrega expirou em " + codigo.getExpiraEm() + ".");
            registrarEvento(reserva, transportador, TipoEventoTransporte.TRANSPORTADOR_SUSPENSO,
                    "Conta do transportador #" + transportador.getId() + " suspensa automaticamente.");
            registrarEvento(reserva, transportador, TipoEventoTransporte.OCORRENCIA_REGISTRADA,
                    "Ocorrência registrada com situação PENDENTE_INVESTIGACAO para análise administrativa.");

            log.warn("Expiração do código de entrega detectada: reservaId={}, transportadorId={}. Transportador suspenso.",
                    reserva.getId(), transportador.getId());
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void invalidarCodigosAtivos(Long reservaId, TipoCodigoTransporte tipo) {
        List<CodigoTransporte> ativos = codigoTransporteRepository
                .findByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalse(reservaId, tipo);
        for (CodigoTransporte c : ativos) {
            c.invalidar();
        }
        if (!ativos.isEmpty()) {
            codigoTransporteRepository.saveAll(ativos);
        }
    }

    private void registrarEvento(ReservaEntrega reserva, Usuario usuario, TipoEventoTransporte tipo, String detalhes) {
        eventoTransporteRepository.save(new EventoTransporte(reserva, usuario, tipo, detalhes));
    }

    private Map<Long, CodigoTransporte> carregarCodigosAtivosPorReserva(
            List<ReservaEntrega> reservas,
            SituacaoReservaEntrega situacaoAlvo,
            TipoCodigoTransporte tipo) {
        List<Long> ids = reservas.stream()
                .filter(r -> r.getSituacao() == situacaoAlvo)
                .map(ReservaEntrega::getId)
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return codigoTransporteRepository
                .findByReservaIdInAndTipoAndInvalidadoFalseAndUtilizadoFalse(ids, tipo)
                .stream()
                .collect(Collectors.toMap(
                        c -> c.getReserva().getId(),
                        c -> c,
                        (primeiro, segundo) -> primeiro
                ));
    }

    private boolean possuiEndereco(Endereco endereco) {
        if (endereco == null) {
            return false;
        }
        boolean temLogradouro = endereco.getLogradouro() != null && !endereco.getLogradouro().isBlank();
        boolean temCidade = endereco.getCidade() != null && !endereco.getCidade().isBlank();
        return temLogradouro || temCidade;
    }
}
