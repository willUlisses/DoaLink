package com.doalink_api.service;

import com.doalink_api.dtos.request.ConfirmarCodigoTransporteRequest;
import com.doalink_api.dtos.request.ReservaEntregaRequest;
import com.doalink_api.dtos.response.ReservaEntregaResponse;
import com.doalink_api.entities.*;
import com.doalink_api.entities.enums.*;
import com.doalink_api.exception.CodigoInvalidoException;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaEntregaServiceTest {

    @Mock
    private ReservaEntregaRepository reservaRepository;
    @Mock
    private OfertaDoacaoRepository ofertaRepository;
    @Mock
    private ReceptorRepository receptorRepository;
    @Mock
    private TransportadorRepository transportadorRepository;
    @Mock
    private CodigoTransporteRepository codigoTransporteRepository;
    @Mock
    private EventoTransporteRepository eventoTransporteRepository;
    @Mock
    private OcorrenciaTransporteRepository ocorrenciaTransporteRepository;

    private CodigoTransporteService codigoTransporteService;
    private ReservaEntregaService service;

    private Doador doador;
    private Receptor receptor;
    private Transportador transportador;
    private Transportador outroTransportador;
    private OfertaDoacao oferta;

    @BeforeEach
    void setUp() {
        codigoTransporteService = new CodigoTransporteService("segredo-teste-hmac-32-caracteres-minimo", 5, 24);
        service = new ReservaEntregaService(
                reservaRepository,
                ofertaRepository,
                receptorRepository,
                transportadorRepository,
                codigoTransporteRepository,
                eventoTransporteRepository,
                ocorrenciaTransporteRepository,
                codigoTransporteService
        );

        doador = new Doador("11111111111", "Doador", "doador@email.com", "hash", "11999990001", TipoPessoa.FISICA);
        doador.setId(1L);

        receptor = new Receptor();
        receptor.setId(2L);
        receptor.setNome("ONG Esperança");
        receptor.setEmail("ong@email.com");
        receptor.setTelefone("11999990002");
        Endereco endReceptor = new Endereco();
        endReceptor.setLogradouro("Av. Paulista");
        endReceptor.setNumero("1000");
        endReceptor.setBairro("Bela Vista");
        endReceptor.setCidade("São Paulo");
        endReceptor.setEstado("SP");
        endReceptor.setCep("01310100");
        receptor.setEnderecoOrganizacao(endReceptor);

        transportador = new Transportador("22222222222", "Carlos", "carlos@email.com", "hash", "11999990003", TipoPessoa.FISICA);
        transportador.setId(3L);
        transportador.setSituacao(SituacaoTransportador.ATIVO);

        outroTransportador = new Transportador("33333333333", "Marcos", "marcos@email.com", "hash", "11999990004", TipoPessoa.FISICA);
        outroTransportador.setId(4L);
        outroTransportador.setSituacao(SituacaoTransportador.ATIVO);

        Endereco endRetirada = new Endereco();
        endRetirada.setLogradouro("Rua das Flores");
        endRetirada.setNumero("50");
        endRetirada.setBairro("Centro");
        endRetirada.setCidade("Campinas");
        endRetirada.setEstado("SP");
        endRetirada.setCep("13010000");

        oferta = new OfertaDoacao(doador, endRetirada);
        oferta.setId(10L);
        oferta.setSituacao(SituacaoOferta.DISPONIVEL);
        ItemDoacao item = new ItemDoacao("Arroz", TipoItemDoacao.NAO_PERECIVEL, 20.0);
        item.setTipoUnidade(TipoUnidade.KG);
        item.setId(100L);
        oferta.adicionarItem(item);
    }

    private ReservaEntrega reservaBase(SituacaoReservaEntrega situacao) {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setPrecisaTransporte(true);
        r.setSituacao(situacao);
        r.setCriadoEm(LocalDateTime.now().minusHours(2));
        r.setAtualizadoEm(LocalDateTime.now().minusHours(1));
        return r;
    }

    // -------------------------------------------------------------------------
    // Reserva
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("reservar: deve reservar oferta DISPONIVEL com transporte e atualizar oferta para RESERVADA")
    void reservar_deveCriarReservaComTransporte() {
        when(receptorRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(reservaRepository.save(any())).thenAnswer(inv -> {
            ReservaEntrega r = inv.getArgument(0);
            r.setId(50L);
            return r;
        });

        ReservaEntregaResponse resp = service.reservar(2L, new ReservaEntregaRequest(10L, true));

        assertThat(resp.id()).isEqualTo(50L);
        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.RESERVADA);
    }

    @Test
    @DisplayName("reservar: sem transporte deve criar AGUARDANDO_RETIRADA")
    void reservar_semTransporte_deveCriarAguardandoRetirada() {
        when(receptorRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(reservaRepository.save(any())).thenAnswer(inv -> {
            ReservaEntrega r = inv.getArgument(0);
            r.setId(51L);
            return r;
        });

        ReservaEntregaResponse resp = service.reservar(2L, new ReservaEntregaRequest(10L, false));

        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.AGUARDANDO_RETIRADA);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.RESERVADA);
    }

    @Test
    @DisplayName("reservar: deve lançar ConflictException quando oferta não estiver DISPONIVEL")
    void reservar_ofertaIndisponivel_deveLancarConflict() {
        oferta.setSituacao(SituacaoOferta.RESERVADA);
        when(receptorRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));

        assertThatThrownBy(() -> service.reservar(2L, new ReservaEntregaRequest(10L, true)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("reservar: oferta sem endereço vai direto para AGUARDANDO_RETIRADA")
    void reservar_ofertaSemEndereco_deveIrDiretoParaAguardandoRetirada() {
        oferta.setLocalRetirada(null);
        when(receptorRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(reservaRepository.save(any())).thenAnswer(inv -> {
            ReservaEntrega r = inv.getArgument(0);
            r.setId(52L);
            return r;
        });

        ReservaEntregaResponse resp = service.reservar(2L, new ReservaEntregaRequest(10L, true));

        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.AGUARDANDO_RETIRADA);
        assertThat(resp.precisaTransporte()).isFalse();
    }

    // -------------------------------------------------------------------------
    // Listagens e privacidade
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listarAguardandoTransporte: deve ocultar contatos e endereços exatos (apenas cidade/UF)")
    void listarAguardandoTransporte_deveMascararEnderecos() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        when(reservaRepository.findBySituacaoOrderByCriadoEmDesc(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE))
                .thenReturn(List.of(r));

        List<ReservaEntregaResponse> lista = service.listarAguardandoTransporte();

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).doador()).isNull();
        assertThat(lista.get(0).receptor()).isNull();
        assertThat(lista.get(0).enderecoRetirada().cidade()).isEqualTo("Campinas");
        assertThat(lista.get(0).enderecoRetirada().logradouro()).isNull();
        assertThat(lista.get(0).enderecoEntrega().cidade()).isEqualTo("São Paulo");
        assertThat(lista.get(0).enderecoEntrega().logradouro()).isNull();
    }

    // -------------------------------------------------------------------------
    // Aceitação do transporte
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("aceitarTransporte: motorista ativo aceita reserva, registra hora, muda status e libera contatos")
    void aceitarTransporte_deveVincularTransportadorEAguardarAutorizacao() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        when(codigoTransporteRepository.findByTipoAndUtilizadoFalseAndInvalidadoFalseAndExpiraEmBefore(any(), any()))
                .thenReturn(List.of());
        when(transportadorRepository.findById(3L)).thenReturn(Optional.of(transportador));
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservaEntregaResponse resp = service.aceitarTransporte(3L, 50L);

        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.AGUARDANDO_AUTORIZACAO_DOADOR);
        assertThat(resp.transportadorId()).isEqualTo(3L);
        assertThat(resp.aceitoEm()).isNotNull();
        assertThat(resp.doador().email()).isEqualTo("doador@email.com");
        assertThat(resp.receptor().email()).isEqualTo("ong@email.com");
        assertThat(resp.enderecoRetirada().logradouro()).isEqualTo("Rua das Flores");
        assertThat(resp.enderecoEntrega().logradouro()).isEqualTo("Av. Paulista");
        verify(eventoTransporteRepository).save(any(EventoTransporte.class));
    }

    @Test
    @DisplayName("aceitarTransporte: segundo motorista tentando assumir reserva já aceita recebe ConflictException (409)")
    void aceitarTransporte_reservaJaAceita_deveLancarConflict() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_AUTORIZACAO_DOADOR);
        r.setTransportador(transportador);
        when(codigoTransporteRepository.findByTipoAndUtilizadoFalseAndInvalidadoFalseAndExpiraEmBefore(any(), any()))
                .thenReturn(List.of());
        when(transportadorRepository.findById(4L)).thenReturn(Optional.of(outroTransportador));
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.aceitarTransporte(4L, 50L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("aceitarTransporte: motorista suspenso recebe ForbiddenException (403)")
    void aceitarTransporte_motoristaSuspenso_deveLancarForbidden() {
        transportador.setSituacao(SituacaoTransportador.SUSPENSO);
        when(codigoTransporteRepository.findByTipoAndUtilizadoFalseAndInvalidadoFalseAndExpiraEmBefore(any(), any()))
                .thenReturn(List.of());
        when(transportadorRepository.findById(3L)).thenReturn(Optional.of(transportador));

        assertThatThrownBy(() -> service.aceitarTransporte(3L, 50L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("suspensa");
        verify(reservaRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // Autorização pelo doador e confirmação de retirada
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("autorizarInicioTransporte: doador dono autoriza início e recebe código de retirada de 6 dígitos")
    void autorizarInicio_doadorDono_deveGerarCodigoRetirada() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_AUTORIZACAO_DOADOR);
        r.setTransportador(transportador);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(codigoTransporteRepository.findByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalse(
                50L, TipoCodigoTransporte.RETIRADA)).thenReturn(List.of());
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(codigoTransporteRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservaEntregaResponse resp = service.autorizarInicioTransporte(1L, 50L);

        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA);
        assertThat(resp.autorizadoEm()).isNotNull();
        assertThat(resp.codigoRetirada()).matches("^\\d{6}$");
        verify(eventoTransporteRepository, times(2)).save(any(EventoTransporte.class));
    }

    @Test
    @DisplayName("autorizarInicioTransporte: outro usuário que não é o doador dono recebe ForbiddenException")
    void autorizarInicio_terceiro_deveLancarForbidden() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_AUTORIZACAO_DOADOR);
        r.setTransportador(transportador);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.autorizarInicioTransporte(99L, 50L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("confirmarRetirada: código correto avança para EM_TRANSPORTE, invalida código 1 e gera código de entrega de 24h")
    void confirmarRetirada_codigoCorreto_deveAvancarParaEmTransporte() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA);
        r.setTransportador(transportador);
        CodigoTransporte codRetirada = codigoTransporteService.criarCodigo(
                r, transportador, TipoCodigoTransporte.RETIRADA, null);
        String codigoPlano = codigoTransporteService.reconstruirCodigoAtivo(codRetirada, LocalDateTime.now());

        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(codigoTransporteRepository.findFirstByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(
                50L, TipoCodigoTransporte.RETIRADA)).thenReturn(Optional.of(codRetirada));
        when(codigoTransporteRepository.findByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalse(
                50L, TipoCodigoTransporte.ENTREGA)).thenReturn(List.of());
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservaEntregaResponse resp = service.confirmarRetirada(
                3L, 50L, new ConfirmarCodigoTransporteRequest(codigoPlano));

        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.EM_TRANSPORTE);
        assertThat(resp.retiradaConfirmadaEm()).isNotNull();
        assertThat(resp.codigoEntregaExpiraEm()).isNotNull();
        assertThat(resp.codigoRetirada()).isNull();
        assertThat(resp.codigoEntrega()).isNull();
        assertThat(codRetirada.isUtilizado()).isTrue();
        assertThat(codRetirada.isInvalidado()).isTrue();
        verify(codigoTransporteRepository, times(2)).save(any(CodigoTransporte.class));
    }

    @Test
    @DisplayName("confirmarRetirada: código incorreto não altera status, incrementa tentativas e bloqueia após 5 erros")
    void confirmarRetirada_codigoIncorreto_deveRegistrarTentativasEBloquear() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA);
        r.setTransportador(transportador);
        CodigoTransporte codRetirada = codigoTransporteService.criarCodigo(
                r, transportador, TipoCodigoTransporte.RETIRADA, null);
        String codigoPlano = codigoTransporteService.reconstruirCodigoAtivo(codRetirada, LocalDateTime.now());
        String codigoErrado = "000000".equals(codigoPlano) ? "000001" : "000000";

        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(codigoTransporteRepository.findFirstByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(
                50L, TipoCodigoTransporte.RETIRADA)).thenReturn(Optional.of(codRetirada));

        for (int i = 1; i <= 5; i++) {
            assertThatThrownBy(() -> service.confirmarRetirada(
                    3L, 50L, new ConfirmarCodigoTransporteRequest(codigoErrado)))
                    .isInstanceOf(CodigoInvalidoException.class);
        }

        assertThat(r.getSituacao()).isEqualTo(SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA);
        assertThat(codRetirada.getTentativas()).isEqualTo(5);
        assertThat(codRetirada.isBloqueado()).isTrue();
        assertThat(codRetirada.isInvalidado()).isTrue();

        assertThatThrownBy(() -> service.confirmarRetirada(
                3L, 50L, new ConfirmarCodigoTransporteRequest(codigoPlano)))
                .isInstanceOf(CodigoInvalidoException.class)
                .hasMessageContaining("Limite");
    }

    @Test
    @DisplayName("confirmarRetirada: outro motorista recebe ForbiddenException")
    void confirmarRetirada_outroMotorista_deveLancarForbidden() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA);
        r.setTransportador(transportador);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.confirmarRetirada(4L, 50L, new ConfirmarCodigoTransporteRequest("123456")))
                .isInstanceOf(ForbiddenException.class);
    }

    // -------------------------------------------------------------------------
    // Entrega e expiração de 24h com suspensão
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmarEntrega: código válido dentro de 24h conclui reserva e oferta")
    void confirmarEntrega_codigoValido_deveConcluirOperacao() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.EM_TRANSPORTE);
        r.setTransportador(transportador);
        r.setRetiradaConfirmadaEm(LocalDateTime.now().minusHours(2));
        CodigoTransporte codEntrega = codigoTransporteService.criarCodigo(
                r, transportador, TipoCodigoTransporte.ENTREGA, LocalDateTime.now().plusHours(22));
        String codigoPlano = codigoTransporteService.reconstruirCodigoAtivo(codEntrega, LocalDateTime.now());

        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(codigoTransporteRepository.findFirstByReservaIdAndTipoOrderByCriadoEmDesc(
                50L, TipoCodigoTransporte.ENTREGA)).thenReturn(Optional.of(codEntrega));
        when(reservaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReservaEntregaResponse resp = service.confirmarEntrega(
                3L, 50L, new ConfirmarCodigoTransporteRequest(codigoPlano));

        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.CONCLUIDA);
        assertThat(resp.entregaConfirmadaEm()).isNotNull();
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.CONCLUIDA);
        assertThat(codEntrega.isUtilizado()).isTrue();
    }

    @Test
    @DisplayName("confirmarEntrega: não permite concluir sem retirada confirmada anteriormente")
    void confirmarEntrega_semRetiradaPrevia_deveLancarConflict() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA);
        r.setTransportador(transportador);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.confirmarEntrega(3L, 50L, new ConfirmarCodigoTransporteRequest("123456")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("confirmarEntrega: código expirado (>24h) bloqueia operação, suspende transportador e cria ocorrência")
    void confirmarEntrega_codigoExpirado_deveSuspenderTransportadorECriarOcorrencia() {
        oferta.setSituacao(SituacaoOferta.RESERVADA);
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.EM_TRANSPORTE);
        r.setTransportador(transportador);
        r.setAceitoEm(LocalDateTime.now().minusHours(30));
        r.setAutorizadoEm(LocalDateTime.now().minusHours(28));
        r.setRetiradaConfirmadaEm(LocalDateTime.now().minusHours(25));
        CodigoTransporte codEntrega = codigoTransporteService.criarCodigo(
                r, transportador, TipoCodigoTransporte.ENTREGA, LocalDateTime.now().minusHours(1));
        String codigoExpiradoPlano = "123456";

        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(codigoTransporteRepository.findFirstByReservaIdAndTipoOrderByCriadoEmDesc(
                50L, TipoCodigoTransporte.ENTREGA)).thenReturn(Optional.of(codEntrega));
        when(ocorrenciaTransporteRepository.existsByReservaId(50L)).thenReturn(false);

        assertThatThrownBy(() -> service.confirmarEntrega(
                3L, 50L, new ConfirmarCodigoTransporteRequest(codigoExpiradoPlano)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("investigação");

        assertThat(r.getSituacao()).isEqualTo(SituacaoReservaEntrega.EM_INVESTIGACAO);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.RESERVADA);
        assertThat(transportador.getSituacao()).isEqualTo(SituacaoTransportador.SUSPENSO);
        assertThat(transportador.getSuspensoEm()).isNotNull();
        assertThat(codEntrega.isInvalidado()).isTrue();
        verify(ocorrenciaTransporteRepository).save(argThat(oc ->
                oc.getSituacao() == SituacaoOcorrenciaTransporte.PENDENTE_INVESTIGACAO
                        && oc.getTransportador().getId().equals(3L)
                        && oc.getReserva().getId().equals(50L)
        ));
    }

    @Test
    @DisplayName("listarMinhasReceptor e buscarPorId: receptor vê código de entrega, mas terceiro recebe Forbidden")
    void buscarPorId_privacidadeDosCodigos() {
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.EM_TRANSPORTE);
        r.setTransportador(transportador);
        CodigoTransporte codEntrega = codigoTransporteService.criarCodigo(
                r, transportador, TipoCodigoTransporte.ENTREGA, LocalDateTime.now().plusHours(20));

        when(codigoTransporteRepository.findByTipoAndUtilizadoFalseAndInvalidadoFalseAndExpiraEmBefore(any(), any()))
                .thenReturn(List.of());
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(codigoTransporteRepository.findFirstByReservaIdAndTipoAndInvalidadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(
                50L, TipoCodigoTransporte.ENTREGA)).thenReturn(Optional.of(codEntrega));
        when(eventoTransporteRepository.findByReservaIdOrderByCriadoEmAsc(50L)).thenReturn(List.of());

        ReservaEntregaResponse paraReceptor = service.buscarPorId(2L, 50L);
        assertThat(paraReceptor.codigoEntrega()).matches("^\\d{6}$");
        assertThat(paraReceptor.codigoRetirada()).isNull();

        ReservaEntregaResponse paraTransportador = service.buscarPorId(3L, 50L);
        assertThat(paraTransportador.codigoEntrega()).isNull();
        assertThat(paraTransportador.codigoRetirada()).isNull();
        assertThat(paraTransportador.codigoEntregaExpiraEm()).isNotNull();

        assertThatThrownBy(() -> service.buscarPorId(999L, 50L))
                .isInstanceOf(ForbiddenException.class);
    }

    // -------------------------------------------------------------------------
    // Concluir retirada direta e cancelar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("concluir: receptor conclui em AGUARDANDO_RETIRADA; operação com transporte exige códigos")
    void concluir_regras() {
        ReservaEntrega retiradaDireta = reservaBase(SituacaoReservaEntrega.AGUARDANDO_RETIRADA);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(retiradaDireta));
        service.concluir(2L, 50L);
        assertThat(retiradaDireta.getSituacao()).isEqualTo(SituacaoReservaEntrega.CONCLUIDA);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.CONCLUIDA);

        ReservaEntrega emTransporte = reservaBase(SituacaoReservaEntrega.EM_TRANSPORTE);
        when(reservaRepository.findWithDetalhesById(51L)).thenReturn(Optional.of(emTransporte));
        assertThatThrownBy(() -> service.concluir(3L, 51L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("cancelar: receptor cancela antes do aceite e não cancela após aceite")
    void cancelar_regras() {
        oferta.setSituacao(SituacaoOferta.RESERVADA);
        ReservaEntrega r = reservaBase(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        service.cancelar(2L, 50L);
        assertThat(r.getSituacao()).isEqualTo(SituacaoReservaEntrega.CANCELADA);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.DISPONIVEL);

        ReservaEntrega aceita = reservaBase(SituacaoReservaEntrega.AGUARDANDO_AUTORIZACAO_DOADOR);
        when(reservaRepository.findWithDetalhesById(51L)).thenReturn(Optional.of(aceita));
        assertThatThrownBy(() -> service.cancelar(2L, 51L))
                .isInstanceOf(ConflictException.class);
    }
}
