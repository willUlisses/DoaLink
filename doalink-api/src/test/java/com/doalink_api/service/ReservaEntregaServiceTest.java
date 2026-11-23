package com.doalink_api.service;

import com.doalink_api.dtos.request.ReservaEntregaRequest;
import com.doalink_api.dtos.request.VincularTransporteRequest;
import com.doalink_api.dtos.response.ReservaEntregaResponse;
import com.doalink_api.entities.*;
import com.doalink_api.entities.enums.*;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.DisponibilidadeTransporteRepository;
import com.doalink_api.repository.OfertaDoacaoRepository;
import com.doalink_api.repository.ReceptorRepository;
import com.doalink_api.repository.ReservaEntregaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalDate;
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
    private DisponibilidadeTransporteRepository disponibilidadeRepository;

    @InjectMocks
    private ReservaEntregaService service;

    private Doador doador;
    private Receptor receptor;
    private Transportador transportador;
    private OfertaDoacao oferta;
    private DisponibilidadeTransporte disponibilidade;

    @BeforeEach
    void setUp() {
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

        disponibilidade = new DisponibilidadeTransporte();
        disponibilidade.setId(20L);
        disponibilidade.setTransportador(transportador);
        disponibilidade.setTipoVeiculo(TipoVeiculo.UTILITARIO_LEVE);
        disponibilidade.setCapacidade(100.0);
        disponibilidade.setCapacidadeRestante(100.0);
        disponibilidade.setTipoUnidade(TipoUnidade.KG);
        disponibilidade.setDataDisponivel(LocalDate.now().plusDays(1));
        disponibilidade.setSituacao(SituacaoDisponibilidade.DISPONIVEL);
    }

    @Test
    @DisplayName("reservar: deve reservar oferta DISPONIVEL com transporte e atualizar oferta para RESERVADA")
    void reservar_deveCriarReservaComTransporte() {
        when(receptorRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(reservaRepository.save(any())).thenAnswer(inv -> {
            ReservaEntrega r = inv.getArgument(0);
            r.setId(50L);
            r.setCriadoEm(LocalDateTime.now());
            r.setAtualizadoEm(LocalDateTime.now());
            return r;
        });

        ReservaEntregaResponse resp = service.reservar(2L, new ReservaEntregaRequest(10L, true));

        assertThat(resp.id()).isEqualTo(50L);
        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.RESERVADA);
    }

    @Test
    @DisplayName("reservar: deve lançar ConflictException quando oferta não estiver DISPONIVEL")
    void reservar_deveLancarConflictQuandoOfertaIndisponivel() {
        oferta.setSituacao(SituacaoOferta.RESERVADA);
        when(receptorRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));

        assertThatThrownBy(() -> service.reservar(2L, new ReservaEntregaRequest(10L, true)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("listarAguardandoTransporte: deve ocultar endereços exatos (apenas cidade/UF)")
    void listarAguardandoTransporte_deveMascararEnderecos() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setPrecisaTransporte(true);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
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

    @Test
    @DisplayName("vincularTransporte: deve vincular reserva, abater capacidadeRestante e permitir múltiplas ofertas")
    void vincularTransporte_deveVincularEAbaterCapacidade() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setPrecisaTransporte(true);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);

        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(disponibilidadeRepository.findWithTransportadorById(20L)).thenReturn(Optional.of(disponibilidade));

        ReservaEntregaResponse resp = service.vincularTransporte(3L, 50L, new VincularTransporteRequest(20L));

        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.TRANSPORTE_VINCULADO);
        assertThat(resp.disponibilidadeId()).isEqualTo(20L);
        assertThat(disponibilidade.getCapacidadeRestante()).isEqualTo(80.0);
        assertThat(disponibilidade.getSituacao()).isEqualTo(SituacaoDisponibilidade.RESERVADA);
        assertThat(resp.enderecoRetirada().logradouro()).isEqualTo("Rua das Flores");
        assertThat(resp.enderecoEntrega().logradouro()).isEqualTo("Av. Paulista");
    }

    @Test
    @DisplayName("vincularTransporte: deve lançar ConflictException quando capacidade restante for insuficiente")
    void vincularTransporte_deveLancarConflictQuandoCapacidadeInsuficiente() {
        disponibilidade.setCapacidadeRestante(10.0);
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);

        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(disponibilidadeRepository.findWithTransportadorById(20L)).thenReturn(Optional.of(disponibilidade));

        assertThatThrownBy(() -> service.vincularTransporte(3L, 50L, new VincularTransporteRequest(20L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Capacidade");
    }

    @Test
    @DisplayName("vincularTransporte: deve lançar ConflictException quando unidade dos itens for incompatível")
    void vincularTransporte_deveLancarConflictQuandoUnidadeIncompativel() {
        disponibilidade.setTipoUnidade(TipoUnidade.CAIXAS);
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);

        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(disponibilidadeRepository.findWithTransportadorById(20L)).thenReturn(Optional.of(disponibilidade));

        assertThatThrownBy(() -> service.vincularTransporte(3L, 50L, new VincularTransporteRequest(20L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Unidade");
    }

    @Test
    @DisplayName("concluir: transportador vinculado conclui reserva e oferta")
    void concluir_transportadorConcluiEntrega() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setDisponibilidade(disponibilidade);
        r.setSituacao(SituacaoReservaEntrega.TRANSPORTE_VINCULADO);

        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        service.concluir(3L, 50L);

        assertThat(r.getSituacao()).isEqualTo(SituacaoReservaEntrega.CONCLUIDA);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.CONCLUIDA);
    }

    @Test
    @DisplayName("cancelar: receptor cancela reserva e devolve oferta para DISPONIVEL")
    void cancelar_receptorCancelaEDevolveOferta() {
        oferta.setSituacao(SituacaoOferta.RESERVADA);
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);

        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        service.cancelar(2L, 50L);

        assertThat(r.getSituacao()).isEqualTo(SituacaoReservaEntrega.CANCELADA);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.DISPONIVEL);
    }

    @Test
    @DisplayName("reservar: sem transporte deve criar AGUARDANDO_RETIRADA e atualizar oferta para RESERVADA")
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
    @DisplayName("reservar: deve lançar ForbiddenException quando usuário não for receptor")
    void reservar_usuarioNaoReceptor_deveLancarForbidden() {
        when(receptorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reservar(99L, new ReservaEntregaRequest(10L, true)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("reservar: deve lançar NotFoundException quando oferta não existir")
    void reservar_ofertaInexistente_deveLancarNotFound() {
        when(receptorRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(ofertaRepository.findWithItensById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reservar(2L, new ReservaEntregaRequest(999L, true)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("listarMinhasDoador: deve retornar reservas das ofertas do doador com dados completos")
    void listarMinhasDoador_deveRetornarComDadosCompletos() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setDisponibilidade(disponibilidade);
        r.setSituacao(SituacaoReservaEntrega.TRANSPORTE_VINCULADO);
        when(reservaRepository.findByOfertaDoadorIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(r));

        List<ReservaEntregaResponse> lista = service.listarMinhasDoador(1L);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).receptor()).isNotNull();
        assertThat(lista.get(0).transportador()).isNotNull();
        assertThat(lista.get(0).enderecoRetirada().logradouro()).isEqualTo("Rua das Flores");
    }

    @Test
    @DisplayName("vincularTransporte: deve lançar ConflictException quando reserva não está AGUARDANDO_TRANSPORTE")
    void vincular_reservaNaoAguardandoTransporte_deveLancarConflict() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_RETIRADA);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.vincularTransporte(3L, 50L, new VincularTransporteRequest(20L)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("vincularTransporte: deve lançar ForbiddenException quando disponibilidade pertence a outro transportador")
    void vincular_disponibilidadeDeOutroTransportador_deveLancarForbidden() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(disponibilidadeRepository.findWithTransportadorById(20L)).thenReturn(Optional.of(disponibilidade));

        assertThatThrownBy(() -> service.vincularTransporte(99L, 50L, new VincularTransporteRequest(20L)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("vincularTransporte: deve lançar ConflictException quando disponibilidade está CANCELADA")
    void vincular_disponibilidadeCancelada_deveLancarConflict() {
        disponibilidade.setSituacao(SituacaoDisponibilidade.CANCELADA);
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(disponibilidadeRepository.findWithTransportadorById(20L)).thenReturn(Optional.of(disponibilidade));

        assertThatThrownBy(() -> service.vincularTransporte(3L, 50L, new VincularTransporteRequest(20L)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("vincularTransporte: deve lançar ConflictException quando disponibilidade está com data passada")
    void vincular_disponibilidadeExpirada_deveLancarConflict() {
        disponibilidade.setDataDisponivel(LocalDate.now().minusDays(1));
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(disponibilidadeRepository.findWithTransportadorById(20L)).thenReturn(Optional.of(disponibilidade));

        assertThatThrownBy(() -> service.vincularTransporte(3L, 50L, new VincularTransporteRequest(20L)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("expirada");
    }

    @Test
    @DisplayName("vincularTransporte: deve permitir quando carga é exatamente igual à capacidade restante")
    void vincular_capacidadeExata_devePermitir() {
        disponibilidade.setCapacidadeRestante(20.0);
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));
        when(disponibilidadeRepository.findWithTransportadorById(20L)).thenReturn(Optional.of(disponibilidade));

        ReservaEntregaResponse resp = service.vincularTransporte(3L, 50L, new VincularTransporteRequest(20L));

        assertThat(resp.situacao()).isEqualTo(SituacaoReservaEntrega.TRANSPORTE_VINCULADO);
        assertThat(disponibilidade.getCapacidadeRestante()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("concluir: receptor conclui em AGUARDANDO_RETIRADA")
    void concluir_receptorEmAguardandoRetirada_deveConcluir() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_RETIRADA);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        service.concluir(2L, 50L);

        assertThat(r.getSituacao()).isEqualTo(SituacaoReservaEntrega.CONCLUIDA);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.CONCLUIDA);
    }

    @Test
    @DisplayName("concluir: transportador não vinculado recebe ForbiddenException")
    void concluir_transportadorNaoVinculado_deveLancarForbidden() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setDisponibilidade(disponibilidade);
        r.setSituacao(SituacaoReservaEntrega.TRANSPORTE_VINCULADO);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.concluir(99L, 50L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("concluir: receptor dono conclui em TRANSPORTE_VINCULADO")
    void concluir_receptorEmTransporteVinculado_deveConcluir() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setDisponibilidade(disponibilidade);
        r.setSituacao(SituacaoReservaEntrega.TRANSPORTE_VINCULADO);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        service.concluir(2L, 50L);

        assertThat(r.getSituacao()).isEqualTo(SituacaoReservaEntrega.CONCLUIDA);
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.CONCLUIDA);
    }

    @Test
    @DisplayName("concluir: terceiro não vinculado em TRANSPORTE_VINCULADO recebe ForbiddenException")
    void concluir_terceiroEmTransporteVinculado_deveLancarForbidden() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setOferta(oferta);
        r.setReceptor(receptor);
        r.setDisponibilidade(disponibilidade);
        r.setSituacao(SituacaoReservaEntrega.TRANSPORTE_VINCULADO);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.concluir(999L, 50L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("concluir: reserva em AGUARDANDO_TRANSPORTE recebe ConflictException")
    void concluir_reservaAguardandoTransporte_deveLancarConflict() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setReceptor(receptor);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.concluir(2L, 50L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("reservar: oferta sem endereço não passa por AGUARDANDO_TRANSPORTE e vai direto para AGUARDANDO_RETIRADA")
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
        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.RESERVADA);
    }

    @Test
    @DisplayName("concluir: reserva CANCELADA recebe ConflictException")
    void concluir_reservaCancelada_deveLancarConflict() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setSituacao(SituacaoReservaEntrega.CANCELADA);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.concluir(2L, 50L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("cancelar: não dono recebe ForbiddenException")
    void cancelar_naoDono_deveLancarForbidden() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setReceptor(receptor);
        r.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.cancelar(99L, 50L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("cancelar: reserva em TRANSPORTE_VINCULADO recebe ConflictException")
    void cancelar_transporteVinculado_deveLancarConflict() {
        ReservaEntrega r = new ReservaEntrega();
        r.setId(50L);
        r.setReceptor(receptor);
        r.setSituacao(SituacaoReservaEntrega.TRANSPORTE_VINCULADO);
        when(reservaRepository.findWithDetalhesById(50L)).thenReturn(Optional.of(r));

        assertThatThrownBy(() -> service.cancelar(2L, 50L))
                .isInstanceOf(ConflictException.class);
    }
}
