package com.doalink_api.service;

import com.doalink_api.dtos.request.DisponibilidadeTransporteRequest;
import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.response.DisponibilidadeTransporteResponse;
import com.doalink_api.entities.DisponibilidadeTransporte;
import com.doalink_api.entities.Endereco;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.enums.SituacaoDisponibilidade;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.entities.enums.TipoVeiculo;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.DisponibilidadeTransporteRepository;
import com.doalink_api.repository.ReservaEntregaRepository;
import com.doalink_api.repository.TransportadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeTransporteServiceTest {

    @Mock
    private DisponibilidadeTransporteRepository disponibilidadeRepository;
    @Mock
    private TransportadorRepository transportadorRepository;
    @Mock
    private ReservaEntregaRepository reservaRepository;

    @InjectMocks
    private DisponibilidadeTransporteService service;

    private Transportador transportador;
    private EnderecoRequest origemReq;
    private LocalDate dataFutura;

    @BeforeEach
    void setUp() {
        transportador = new Transportador(
                "12345678901", "Carlos Transportes", "carlos@email.com", "hash", "11999999999", TipoPessoa.FISICA);
        transportador.setId(1L);

        origemReq = new EnderecoRequest(
                "Rua das Flores", "100", "Galpão A", "Centro", "São Paulo", "sp", "01310-100");
        dataFutura = LocalDate.now().plusDays(3);
    }

    private DisponibilidadeTransporteRequest reqValida(LocalTime horario) {
        return new DisponibilidadeTransporteRequest(
                TipoVeiculo.UTILITARIO_LEVE,
                500.0,
                TipoUnidade.KG,
                origemReq,
                dataFutura,
                horario
        );
    }

    private DisponibilidadeTransporte entidadeExemplo(Long id, SituacaoDisponibilidade situacao) {
        Endereco origem = new Endereco();
        origem.setLogradouro("Rua das Flores");
        origem.setNumero("100");
        origem.setComplemento("Galpão A");
        origem.setBairro("Centro");
        origem.setCidade("São Paulo");
        origem.setEstado("SP");
        origem.setCep("01310100");

        DisponibilidadeTransporte d = new DisponibilidadeTransporte();
        d.setId(id);
        d.setTransportador(transportador);
        d.setTipoVeiculo(TipoVeiculo.UTILITARIO_LEVE);
        d.setCapacidade(500.0);
        d.setCapacidadeRestante(500.0);
        d.setTipoUnidade(TipoUnidade.KG);
        d.setOrigem(origem);
        d.setDataDisponivel(dataFutura);
        d.setHorarioDisponivel(LocalTime.of(8, 30));
        d.setSituacao(situacao);
        d.setCriadoEm(LocalDateTime.now());
        d.setAtualizadoEm(LocalDateTime.now());
        return d;
    }

    @Test
    @DisplayName("criar: deve salvar disponibilidade normalizada e retornar response")
    void criar_deveSalvarERetornarResponse() {
        DisponibilidadeTransporteRequest req = reqValida(LocalTime.of(8, 30));
        when(transportadorRepository.findById(1L)).thenReturn(Optional.of(transportador));
        when(disponibilidadeRepository.existsByTransportadorIdAndDataDisponivelAndSituacaoIn(
                eq(1L), eq(dataFutura), anyCollection())).thenReturn(false);
        when(disponibilidadeRepository.save(any())).thenReturn(entidadeExemplo(10L, SituacaoDisponibilidade.DISPONIVEL));

        DisponibilidadeTransporteResponse resp = service.criar(1L, req);

        ArgumentCaptor<DisponibilidadeTransporte> captor = ArgumentCaptor.forClass(DisponibilidadeTransporte.class);
        verify(disponibilidadeRepository).save(captor.capture());
        DisponibilidadeTransporte salvo = captor.getValue();

        assertThat(salvo.getCapacidadeRestante()).isEqualTo(500.0);
        assertThat(salvo.getOrigem().getCep()).isEqualTo("01310100");
        assertThat(salvo.getOrigem().getEstado()).isEqualTo("SP");
        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.situacao()).isEqualTo(SituacaoDisponibilidade.DISPONIVEL);
        assertThat(resp.capacidadeRestante()).isEqualTo(500.0);
        assertThat(resp.horarioDisponivel()).isEqualTo(LocalTime.of(8, 30));
    }

    @Test
    @DisplayName("criar: deve permitir horário opcional nulo")
    void criar_deveAceitarHorarioNulo() {
        DisponibilidadeTransporteRequest req = reqValida(null);
        DisponibilidadeTransporte salvo = entidadeExemplo(11L, SituacaoDisponibilidade.DISPONIVEL);
        salvo.setHorarioDisponivel(null);

        when(transportadorRepository.findById(1L)).thenReturn(Optional.of(transportador));
        when(disponibilidadeRepository.existsByTransportadorIdAndDataDisponivelAndSituacaoIn(
                eq(1L), eq(dataFutura), anyCollection())).thenReturn(false);
        when(disponibilidadeRepository.save(any())).thenReturn(salvo);

        DisponibilidadeTransporteResponse resp = service.criar(1L, req);

        assertThat(resp.horarioDisponivel()).isNull();
    }

    @Test
    @DisplayName("criar: deve lançar ForbiddenException quando usuário não é transportador")
    void criar_deveLancarForbiddenQuandoNaoEhTransportador() {
        when(transportadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(99L, reqValida(null)))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("transportadores");
        verify(disponibilidadeRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: deve lançar ConflictException quando já existe disponibilidade ativa na mesma data")
    void criar_deveLancarConflictQuandoDataJaOcupada() {
        when(transportadorRepository.findById(1L)).thenReturn(Optional.of(transportador));
        when(disponibilidadeRepository.existsByTransportadorIdAndDataDisponivelAndSituacaoIn(
                eq(1L), eq(dataFutura), anyCollection())).thenReturn(true);

        assertThatThrownBy(() -> service.criar(1L, reqValida(null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(dataFutura.toString());
        verify(disponibilidadeRepository, never()).save(any());
    }

    @Test
    @DisplayName("listarMinhas: deve retornar disponibilidades do transportador")
    void listarMinhas_deveRetornarListaDoTransportador() {
        when(disponibilidadeRepository.findByTransportadorIdOrderByDataDisponivelAscCriadoEmDesc(1L))
                .thenReturn(List.of(entidadeExemplo(10L, SituacaoDisponibilidade.DISPONIVEL)));

        List<DisponibilidadeTransporteResponse> lista = service.listarMinhas(1L);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).id()).isEqualTo(10L);
    }

    @Test
    @DisplayName("cancelar: deve alterar situação para CANCELADA quando dono e DISPONIVEL")
    void cancelar_deveCancelarQuandoDonoEDisponivel() {
        DisponibilidadeTransporte d = entidadeExemplo(10L, SituacaoDisponibilidade.DISPONIVEL);
        when(disponibilidadeRepository.findWithTransportadorById(10L)).thenReturn(Optional.of(d));

        service.cancelar(1L, 10L);

        assertThat(d.getSituacao()).isEqualTo(SituacaoDisponibilidade.CANCELADA);
    }

    @Test
    @DisplayName("cancelar: deve lançar NotFoundException quando id não existir")
    void cancelar_deveLancarNotFoundQuandoInexistente() {
        when(disponibilidadeRepository.findWithTransportadorById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(1L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("cancelar: deve lançar ForbiddenException quando usuário não for o dono")
    void cancelar_deveLancarForbiddenQuandoNaoEhDono() {
        DisponibilidadeTransporte d = entidadeExemplo(10L, SituacaoDisponibilidade.DISPONIVEL);
        when(disponibilidadeRepository.findWithTransportadorById(10L)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> service.cancelar(2L, 10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("permissão");
        assertThat(d.getSituacao()).isEqualTo(SituacaoDisponibilidade.DISPONIVEL);
    }

    @Test
    @DisplayName("cancelar: deve lançar ForbiddenException quando situação não for DISPONIVEL")
    void cancelar_deveLancarForbiddenQuandoSituacaoNaoDisponivel() {
        DisponibilidadeTransporte d = entidadeExemplo(10L, SituacaoDisponibilidade.RESERVADA);
        when(disponibilidadeRepository.findWithTransportadorById(10L)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> service.cancelar(1L, 10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("DISPONIVEL");
    }

    @Test
    @DisplayName("encerrar: deve lançar ConflictException quando houver entregas vinculadas pendentes")
    void encerrar_comEntregasPendentes_deveLancarConflict() {
        DisponibilidadeTransporte d = entidadeExemplo(10L, SituacaoDisponibilidade.RESERVADA);
        when(disponibilidadeRepository.findWithTransportadorById(10L)).thenReturn(Optional.of(d));
        when(reservaRepository.existsByDisponibilidadeIdAndSituacao(10L, SituacaoReservaEntrega.TRANSPORTE_VINCULADO))
                .thenReturn(true);

        assertThatThrownBy(() -> service.encerrar(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("não concluídas");
        assertThat(d.getSituacao()).isEqualTo(SituacaoDisponibilidade.RESERVADA);
    }

    @Test
    @DisplayName("encerrar: deve alterar situação para CONCLUIDA quando RESERVADA e sem pendências")
    void encerrar_semPendencias_deveConcluir() {
        DisponibilidadeTransporte d = entidadeExemplo(10L, SituacaoDisponibilidade.RESERVADA);
        when(disponibilidadeRepository.findWithTransportadorById(10L)).thenReturn(Optional.of(d));
        when(reservaRepository.existsByDisponibilidadeIdAndSituacao(10L, SituacaoReservaEntrega.TRANSPORTE_VINCULADO))
                .thenReturn(false);

        service.encerrar(1L, 10L);

        assertThat(d.getSituacao()).isEqualTo(SituacaoDisponibilidade.CONCLUIDA);
    }

    @Test
    @DisplayName("encerrar: deve lançar ConflictException quando disponibilidade estiver DISPONIVEL")
    void encerrar_disponivel_deveLancarConflict() {
        DisponibilidadeTransporte d = entidadeExemplo(10L, SituacaoDisponibilidade.DISPONIVEL);
        when(disponibilidadeRepository.findWithTransportadorById(10L)).thenReturn(Optional.of(d));

        assertThatThrownBy(() -> service.encerrar(1L, 10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("RESERVADAS");
    }
}

