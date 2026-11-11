package com.doalink_api.service;

import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.request.ItemNecessidadeRequest;
import com.doalink_api.dtos.request.NecessidadeDoacaoRequest;
import com.doalink_api.dtos.response.NecessidadeDoacaoResponse;
import com.doalink_api.entities.ItemDoacao;
import com.doalink_api.entities.NecessidadeDoacao;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.enums.SituacaoNecessidade;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.NecessidadeDoacaoRepository;
import com.doalink_api.repository.ReceptorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NecessidadeDoacaoServiceTest {

    @Mock
    private NecessidadeDoacaoRepository necessidadeRepository;

    @Mock
    private ReceptorRepository receptorRepository;

    @InjectMocks
    private NecessidadeDoacaoService service;

    private Receptor receptor;
    private ItemNecessidadeRequest itemReq;
    private EnderecoRequest enderecoReq;

    @BeforeEach
    void setUp() {
        receptor = new Receptor();
        receptor.setId(1L);
        receptor.setNome("ONG Esperança");
        receptor.setEmail("ong@esperanca.org");
        receptor.setTelefone("11988887777");

        itemReq = new ItemNecessidadeRequest(
                "Feijão Carioca",
                TipoItemDoacao.NAO_PERECIVEL,
                TipoUnidade.KG,
                10.0);

        enderecoReq = new EnderecoRequest(
                "Rua da Paz",
                "123",
                "Galpão",
                "Bela Vista",
                "São Paulo",
                "SP",
                "01310-100");
    }

    @Test
    @DisplayName("criar: receptor válido deve salvar e retornar response com sucesso")
    void criar_receptorValido_deveSalvarERetornarResponse() {
        var req = new NecessidadeDoacaoRequest(List.of(itemReq), enderecoReq);

        when(receptorRepository.findById(1L)).thenReturn(Optional.of(receptor));
        when(necessidadeRepository.save(any(NecessidadeDoacao.class))).thenAnswer(invocation -> {
            NecessidadeDoacao n = invocation.getArgument(0);
            n.setId(10L);
            return n;
        });

        NecessidadeDoacaoResponse response = service.criar(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.situacao()).isEqualTo(SituacaoNecessidade.ABERTA);
        assertThat(response.receptor().nome()).isEqualTo("ONG Esperança");
        assertThat(response.localEntrega().cidade()).isEqualTo("São Paulo");
        assertThat(response.itens()).hasSize(1);
        assertThat(response.itens().get(0).tipoUnidade()).isEqualTo(TipoUnidade.KG);
        assertThat(response.itens().get(0).quantidade()).isEqualTo(10.0);

        verify(receptorRepository).findById(1L);
        verify(necessidadeRepository).save(any(NecessidadeDoacao.class));
    }

    @Test
    @DisplayName("criar: usuário não receptor deve lançar ForbiddenException")
    void criar_usuarioNaoReceptor_deveLancarForbiddenException() {
        var req = new NecessidadeDoacaoRequest(List.of(itemReq), enderecoReq);

        when(receptorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(99L, req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Apenas receptores podem criar necessidades");

        verify(necessidadeRepository, never()).save(any());
    }

    @Test
    @DisplayName("listarMinhas: deve retornar lista de necessidades do receptor")
    void listarMinhas_deveRetornarListaDoReceptor() {
        NecessidadeDoacao n = new NecessidadeDoacao();
        n.setId(1L);
        n.setReceptor(receptor);
        n.setLocalEntrega(com.doalink_api.mapper.EnderecoMapper.toEndereco(enderecoReq));
        ItemDoacao item = new ItemDoacao("Arroz", TipoItemDoacao.NAO_PERECIVEL, 5.0);
        item.setTipoUnidade(TipoUnidade.KG);
        n.adicionarItem(item);

        when(necessidadeRepository.findByReceptorIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(n));

        List<NecessidadeDoacaoResponse> resultado = service.listarMinhas(1L);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).id()).isEqualTo(1L);
        verify(necessidadeRepository).findByReceptorIdOrderByCriadoEmDesc(1L);
    }

    @Test
    @DisplayName("listarPorSituacao: deve retornar página filtrada pela situação informada")
    void listarPorSituacao_deveRetornarPagina() {
        Pageable pageable = PageRequest.of(0, 10);
        NecessidadeDoacao n = new NecessidadeDoacao();
        n.setId(1L);
        n.setReceptor(receptor);
        n.setLocalEntrega(com.doalink_api.mapper.EnderecoMapper.toEndereco(enderecoReq));

        when(necessidadeRepository.findBySituacao(SituacaoNecessidade.ABERTA, pageable))
                .thenReturn(new PageImpl<>(List.of(n), pageable, 1));

        Page<NecessidadeDoacaoResponse> resultado = service.listarPorSituacao(SituacaoNecessidade.ABERTA, pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).id()).isEqualTo(1L);
        verify(necessidadeRepository).findBySituacao(SituacaoNecessidade.ABERTA, pageable);
    }

    @Test
    @DisplayName("buscarPorId: existente deve retornar DTO")
    void buscarPorId_existente_deveRetornarResponse() {
        NecessidadeDoacao n = new NecessidadeDoacao();
        n.setId(5L);
        n.setReceptor(receptor);
        n.setLocalEntrega(com.doalink_api.mapper.EnderecoMapper.toEndereco(enderecoReq));

        when(necessidadeRepository.findWithItensById(5L)).thenReturn(Optional.of(n));

        NecessidadeDoacaoResponse response = service.buscarPorId(5L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(5L);
        verify(necessidadeRepository).findWithItensById(5L);
    }

    @Test
    @DisplayName("buscarPorId: inexistente deve lançar NotFoundException")
    void buscarPorId_inexistente_deveLancarNotFoundException() {
        when(necessidadeRepository.findWithItensById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Necessidade não encontrada: 99");
    }
}
