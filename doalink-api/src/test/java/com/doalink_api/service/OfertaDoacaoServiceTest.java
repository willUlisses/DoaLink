package com.doalink_api.service;

import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.request.ItemDoacaoRequest;
import com.doalink_api.dtos.request.OfertaDoacaoRequest;
import com.doalink_api.dtos.response.OfertaDoacaoResponse;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.DisponibilidadeTransporte;
import com.doalink_api.entities.ItemDoacao;
import com.doalink_api.entities.OfertaDoacao;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.ReservaEntrega;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.DoadorRepository;
import com.doalink_api.repository.OfertaDoacaoRepository;
import com.doalink_api.repository.ReservaEntregaRepository;
import com.doalink_api.repository.TransportadorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfertaDoacaoServiceTest {

    @Mock
    private OfertaDoacaoRepository ofertaRepository;
    @Mock
    private DoadorRepository doadorRepository;
    @Mock
    private TransportadorRepository transportadorRepository;
    @Mock
    private ReservaEntregaRepository reservaRepository;
    @InjectMocks
    private OfertaDoacaoService service;

    private Doador doador;
    private ItemDoacaoRequest itemReq;
    private EnderecoRequest enderecoReq;

    @BeforeEach
    void setUp() {
        doador = new Doador("12345678901", "João Silva", "joao@email.com", "hash", "11999999999", TipoPessoa.FISICA);
        doador.setId(1L);

        itemReq = new ItemDoacaoRequest("Arroz tipo 1", TipoItemDoacao.NAO_PERECIVEL, TipoUnidade.KG, 5.0);

        enderecoReq = new EnderecoRequest(
                "Rua das Flores", "10", null, "Centro", "São Paulo", "SP", "01310-100");
    }

    // -------------------------------------------------------------------------
    // criar()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("criar: deve salvar oferta com N itens e retornar response")
    void criar_deveRetornarResponseComItens() {
        OfertaDoacaoRequest req = new OfertaDoacaoRequest(List.of(itemReq, itemReq), enderecoReq);

        OfertaDoacao ofertaSalva = ofertaComNItens(doador, 10L, 2);
        when(doadorRepository.findById(1L)).thenReturn(Optional.of(doador));
        when(ofertaRepository.save(any())).thenReturn(ofertaSalva);

        OfertaDoacaoResponse resp = service.criar(1L, req);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.situacao()).isEqualTo(SituacaoOferta.DISPONIVEL);
        assertThat(resp.itens()).hasSize(2);
        verify(ofertaRepository).save(any(OfertaDoacao.class));
    }

    @Test
    @DisplayName("criar: deve lançar ForbiddenException quando usuário não é doador")
    void criar_deveLancarForbiddenQuandoNaoEhDoador() {
        when(doadorRepository.findById(99L)).thenReturn(Optional.empty());

        OfertaDoacaoRequest req = new OfertaDoacaoRequest(List.of(itemReq), enderecoReq);

        assertThatThrownBy(() -> service.criar(99L, req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("doadores");
        verify(ofertaRepository, never()).save(any());
    }

    @Test
    @DisplayName("criar: deve aceitar oferta sem endereço informado")
    void criar_deveAceitarSemEndereco() {
        OfertaDoacaoRequest req = new OfertaDoacaoRequest(List.of(itemReq), null);

        OfertaDoacao ofertaSalva = ofertaComNItens(doador, 10L, 1);
        ofertaSalva.setLocalRetirada(null);
        when(doadorRepository.findById(1L)).thenReturn(Optional.of(doador));
        when(ofertaRepository.save(any())).thenReturn(ofertaSalva);

        OfertaDoacaoResponse resp = service.criar(1L, req);

        assertThat(resp.localRetirada()).isNull();
        verify(ofertaRepository).save(any());
    }

    // -------------------------------------------------------------------------
    // listarMinhas()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("listarMinhas: deve retornar lista vazia quando doador não tem ofertas")
    void listarMinhas_deveRetornarListaVazia() {
        when(ofertaRepository.findByDoadorIdOrderByCriadoEmDesc(1L)).thenReturn(List.of());

        List<OfertaDoacaoResponse> result = service.listarMinhas(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listarMinhas: deve mapear todas as ofertas do doador")
    void listarMinhas_deveMapearOfertas() {
        OfertaDoacao o1 = ofertaComNItens(doador, 10L, 1);
        OfertaDoacao o2 = ofertaComNItens(doador, 11L, 1);
        when(ofertaRepository.findByDoadorIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(o1, o2));

        List<OfertaDoacaoResponse> result = service.listarMinhas(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(OfertaDoacaoResponse::id).containsExactly(10L, 11L);
    }

    @Test
    @DisplayName("listarMinhas: deve incluir contato da ONG quando oferta estiver reservada")
    void listarMinhas_deveIncluirContatoReceptorQuandoReservada() {
        OfertaDoacao o1 = ofertaComNItens(doador, 10L, 1);
        o1.setSituacao(SituacaoOferta.RESERVADA);

        Receptor receptor = new Receptor();
        receptor.setId(5L);
        receptor.setNome("ONG Solidária");
        receptor.setEmail("contato@ongsolidaria.org");
        receptor.setTelefone("11977778888");

        ReservaEntrega reserva = new ReservaEntrega();
        reserva.setOferta(o1);
        reserva.setReceptor(receptor);
        reserva.setSituacao(SituacaoReservaEntrega.AGUARDANDO_RETIRADA);

        when(ofertaRepository.findByDoadorIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(o1));
        when(reservaRepository.findByOfertaIdInAndSituacaoNot(List.of(10L), SituacaoReservaEntrega.CANCELADA))
                .thenReturn(List.of(reserva));

        List<OfertaDoacaoResponse> result = service.listarMinhas(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).receptor()).isNotNull();
        assertThat(result.get(0).receptor().nome()).isEqualTo("ONG Solidária");
        assertThat(result.get(0).receptor().email()).isEqualTo("contato@ongsolidaria.org");
        assertThat(result.get(0).receptor().telefone()).isEqualTo("11977778888");
    }

    @Test
    @DisplayName("buscarPorId: doador dono visualiza contato da ONG quando oferta tem reserva ativa")
    void buscarPorId_donoVisualizaContatoReceptor() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        oferta.setSituacao(SituacaoOferta.RESERVADA);

        Receptor receptor = new Receptor();
        receptor.setId(5L);
        receptor.setNome("ONG Esperança");
        receptor.setEmail("ong@esperanca.org");
        receptor.setTelefone(null);

        ReservaEntrega reserva = new ReservaEntrega();
        reserva.setOferta(oferta);
        reserva.setReceptor(receptor);
        reserva.setSituacao(SituacaoReservaEntrega.AGUARDANDO_RETIRADA);

        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(reservaRepository.findFirstByOfertaIdAndSituacaoNot(10L, SituacaoReservaEntrega.CANCELADA))
                .thenReturn(Optional.of(reserva));

        OfertaDoacaoResponse resp = service.buscarPorId(10L, 1L);

        assertThat(resp.receptor()).isNotNull();
        assertThat(resp.receptor().nome()).isEqualTo("ONG Esperança");
        assertThat(resp.receptor().email()).isEqualTo("ong@esperanca.org");
        assertThat(resp.receptor().telefone()).isNull();
    }

    // -------------------------------------------------------------------------
    // buscarPorId()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("buscarPorId: deve lançar NotFoundException quando oferta não existe")
    void buscarPorId_deveLancarNotFound() {
        when(ofertaRepository.findWithItensById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("buscarPorId: deve retornar endereço completo para o doador dono")
    void buscarPorId_deveRetornarQuandoExiste() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        oferta.setLocalRetirada(com.doalink_api.mapper.EnderecoMapper.toEndereco(enderecoReq));
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(transportadorRepository.existsById(1L)).thenReturn(false);

        OfertaDoacaoResponse resp = service.buscarPorId(10L, 1L);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.localRetirada().logradouro()).isEqualTo("Rua das Flores");
        assertThat(resp.localRetirada().cep()).isEqualTo("01310100");
    }

    @Test
    @DisplayName("buscarPorId: deve mascarar endereço exato para quem não é o doador dono nem vinculado na reserva")
    void buscarPorId_deveMascararEnderecoParaNaoDono() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        oferta.setLocalRetirada(com.doalink_api.mapper.EnderecoMapper.toEndereco(enderecoReq));
        oferta.setSituacao(SituacaoOferta.RESERVADA);
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(transportadorRepository.existsById(2L)).thenReturn(true);
        when(reservaRepository.findFirstByOfertaIdAndSituacaoNot(10L, SituacaoReservaEntrega.CANCELADA))
                .thenReturn(Optional.empty());

        OfertaDoacaoResponse resp = service.buscarPorId(10L, 2L);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.localRetirada().cidade()).isEqualTo("São Paulo");
        assertThat(resp.localRetirada().estado()).isEqualTo("SP");
        assertThat(resp.localRetirada().logradouro()).isNull();
        assertThat(resp.localRetirada().numero()).isNull();
        assertThat(resp.localRetirada().cep()).isNull();
    }

    @Test
    @DisplayName("buscarPorId: deve liberar endereço completo para o transportador vinculado na reserva")
    void buscarPorId_deveLiberarEnderecoParaTransportadorVinculado() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        oferta.setLocalRetirada(com.doalink_api.mapper.EnderecoMapper.toEndereco(enderecoReq));
        oferta.setSituacao(SituacaoOferta.RESERVADA);

        Transportador transportador = new Transportador();
        transportador.setId(2L);
        DisponibilidadeTransporte disp = new DisponibilidadeTransporte();
        disp.setTransportador(transportador);
        ReservaEntrega reserva = new ReservaEntrega();
        reserva.setDisponibilidade(disp);

        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(reservaRepository.findFirstByOfertaIdAndSituacaoNot(10L, SituacaoReservaEntrega.CANCELADA))
                .thenReturn(Optional.of(reserva));

        OfertaDoacaoResponse resp = service.buscarPorId(10L, 2L);

        assertThat(resp.localRetirada().logradouro()).isEqualTo("Rua das Flores");
        assertThat(resp.localRetirada().cep()).isEqualTo("01310100");
    }

    @Test
    @DisplayName("buscarPorId: transportador pode visualizar oferta RESERVADA")
    void buscarPorId_transportadorPodeVerReservada() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        oferta.setSituacao(SituacaoOferta.RESERVADA);
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(transportadorRepository.existsById(2L)).thenReturn(true);

        OfertaDoacaoResponse resp = service.buscarPorId(10L, 2L);

        assertThat(resp.id()).isEqualTo(10L);
        assertThat(resp.situacao()).isEqualTo(SituacaoOferta.RESERVADA);
    }

    @Test
    @DisplayName("buscarPorId: transportador não pode visualizar oferta DISPONIVEL ou CANCELADA")
    void buscarPorId_transportadorNaoPodeVerNaoReservada() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        oferta.setSituacao(SituacaoOferta.DISPONIVEL);
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(transportadorRepository.existsById(2L)).thenReturn(true);

        assertThatThrownBy(() -> service.buscarPorId(10L, 2L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("RESERVADA");
    }

    @Test
    @DisplayName("buscarPorId: transportador vinculado continua visualizando oferta CONCLUIDA com endereço completo")
    void buscarPorId_transportadorVinculadoPodeVerConcluida() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        oferta.setLocalRetirada(com.doalink_api.mapper.EnderecoMapper.toEndereco(enderecoReq));
        oferta.setSituacao(SituacaoOferta.CONCLUIDA);

        Transportador transportador = new Transportador();
        transportador.setId(2L);
        DisponibilidadeTransporte disp = new DisponibilidadeTransporte();
        disp.setTransportador(transportador);
        ReservaEntrega reserva = new ReservaEntrega();
        reserva.setDisponibilidade(disp);
        reserva.setSituacao(SituacaoReservaEntrega.CONCLUIDA);

        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(reservaRepository.findFirstByOfertaIdAndSituacaoNot(10L, SituacaoReservaEntrega.CANCELADA))
                .thenReturn(Optional.of(reserva));

        OfertaDoacaoResponse resp = service.buscarPorId(10L, 2L);

        assertThat(resp.situacao()).isEqualTo(SituacaoOferta.CONCLUIDA);
        assertThat(resp.localRetirada().logradouro()).isEqualTo("Rua das Flores");
    }

    @Test
    @DisplayName("buscarPorId: transportador não vinculado não pode visualizar oferta CONCLUIDA")
    void buscarPorId_transportadorNaoVinculadoNaoPodeVerConcluida() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        oferta.setSituacao(SituacaoOferta.CONCLUIDA);
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(reservaRepository.findFirstByOfertaIdAndSituacaoNot(10L, SituacaoReservaEntrega.CANCELADA))
                .thenReturn(Optional.empty());
        when(transportadorRepository.existsById(2L)).thenReturn(true);

        assertThatThrownBy(() -> service.buscarPorId(10L, 2L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("listarDisponiveis: doador não pode listar ofertas gerais")
    void listarDisponiveis_doador_deveLancarForbidden() {
        when(doadorRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.listarDisponiveis(
                org.springframework.data.domain.PageRequest.of(0, 10), null, null, null, 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("buscarPorId: doador tentando visualizar oferta de outro doador deve receber Forbidden")
    void buscarPorId_outroDoador_deveLancarForbidden() {
        Doador outro = new Doador("98765432100", "Maria", "maria@email.com", "hash", null, TipoPessoa.FISICA);
        outro.setId(2L);
        OfertaDoacao oferta = ofertaComNItens(outro, 10L, 1);

        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));
        when(doadorRepository.existsById(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.buscarPorId(10L, 1L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("buscarPorId: ONG visualiza oferta DISPONIVEL sem contato completo e bloqueia oferta reservada por outra ONG")
    void buscarPorId_receptor_regrasDeVisualizacao() {
        OfertaDoacao ofertaDisponivel = ofertaComNItens(doador, 10L, 1);
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(ofertaDisponivel));
        when(doadorRepository.existsById(5L)).thenReturn(false);
        when(transportadorRepository.existsById(5L)).thenReturn(false);

        OfertaDoacaoResponse resp = service.buscarPorId(10L, 5L);
        assertThat(resp.doador().nome()).isEqualTo("João Silva");
        assertThat(resp.doador().email()).isNull();

        OfertaDoacao ofertaDeOutraOng = ofertaComNItens(doador, 11L, 1);
        ofertaDeOutraOng.setSituacao(SituacaoOferta.RESERVADA);
        Receptor outraOng = new Receptor();
        outraOng.setId(9L);
        ReservaEntrega reservaOutraOng = new ReservaEntrega();
        reservaOutraOng.setOferta(ofertaDeOutraOng);
        reservaOutraOng.setReceptor(outraOng);

        when(ofertaRepository.findWithItensById(11L)).thenReturn(Optional.of(ofertaDeOutraOng));
        when(reservaRepository.findFirstByOfertaIdAndSituacaoNot(11L, SituacaoReservaEntrega.CANCELADA))
                .thenReturn(Optional.of(reservaOutraOng));

        assertThatThrownBy(() -> service.buscarPorId(11L, 5L))
                .isInstanceOf(ForbiddenException.class);
    }

    // -------------------------------------------------------------------------
    // cancelar()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("cancelar: deve mudar situação para CANCELADA quando doador é dono")
    void cancelar_deveCancelarQuandoDono() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));

        service.cancelar(1L, 10L);

        assertThat(oferta.getSituacao()).isEqualTo(SituacaoOferta.CANCELADA);
    }

    @Test
    @DisplayName("cancelar: deve lançar ForbiddenException quando usuário não é dono")
    void cancelar_deveLancarForbiddenQuandoNaoEhDono() {
        Doador outroDono = new Doador("98765432100", "Maria", "maria@email.com", "hash", null, TipoPessoa.FISICA);
        outroDono.setId(2L);
        OfertaDoacao oferta = ofertaComNItens(outroDono, 10L, 1);
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));

        assertThatThrownBy(() -> service.cancelar(1L, 10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("permissão");
    }

    @Test
    @DisplayName("cancelar: deve lançar ForbiddenException quando oferta não está DISPONIVEL")
    void cancelar_deveLancarForbiddenQuandoNaoDisponivel() {
        OfertaDoacao oferta = ofertaComNItens(doador, 10L, 1);
        oferta.setSituacao(SituacaoOferta.CONCLUIDA);
        when(ofertaRepository.findWithItensById(10L)).thenReturn(Optional.of(oferta));

        assertThatThrownBy(() -> service.cancelar(1L, 10L))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("DISPONIVEL");
    }

    @Test
    @DisplayName("cancelar: deve lançar NotFoundException quando oferta não existe")
    void cancelar_deveLancarNotFoundQuandoOfertaInexistente() {
        when(ofertaRepository.findWithItensById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelar(1L, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private OfertaDoacao ofertaComNItens(Doador dono, Long id, int qtdItens) {
        OfertaDoacao oferta = new OfertaDoacao(dono, null);
        oferta.setId(id);
        oferta.setSituacao(SituacaoOferta.DISPONIVEL);

        for (int i = 0; i < qtdItens; i++) {
            oferta.adicionarItem(new ItemDoacao(
                    "Arroz tipo 1", TipoItemDoacao.NAO_PERECIVEL, 5.0));
        }

        // Simula @PrePersist (campos são private sem setter)
        try {
            var f = OfertaDoacao.class.getDeclaredField("criadoEm");
            f.setAccessible(true);
            f.set(oferta, LocalDateTime.now());
            var f2 = OfertaDoacao.class.getDeclaredField("atualizadoEm");
            f2.setAccessible(true);
            f2.set(oferta, LocalDateTime.now());
        } catch (Exception ignored) {
        }

        return oferta;
    }
}
