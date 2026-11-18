package com.doalink_api.service;

import com.doalink_api.dtos.request.CampanhaEmergencialRequest;
import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.request.ItemCampanhaRequest;
import com.doalink_api.dtos.response.CampanhaEmergencialResponse;
import com.doalink_api.entities.CampanhaEmergencial;
import com.doalink_api.entities.ItemCampanha;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.enums.SituacaoCampanha;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.mapper.EnderecoMapper;
import com.doalink_api.repository.CampanhaEmergencialRepository;
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
class CampanhaEmergencialServiceTest {

    @Mock
    private CampanhaEmergencialRepository campanhaRepository;

    @Mock
    private ReceptorRepository receptorRepository;

    @InjectMocks
    private CampanhaEmergencialService service;

    private Receptor receptor;
    private EnderecoRequest enderecoReq;
    private ItemCampanhaRequest itemReq;

    @BeforeEach
    void setUp() {
        receptor = new Receptor();
        receptor.setId(1L);
        receptor.setNome("ONG Esperança");
        receptor.setEmail("ong@esperanca.org");
        receptor.setTelefone("11988887777");

        enderecoReq = new EnderecoRequest("Rua das Flores", "100", null, "Centro", "São Paulo", "SP", "01001-000");
        itemReq = new ItemCampanhaRequest(TipoItemDoacao.PERECIVEL, TipoUnidade.KG, 50.0, "Arroz e Feijão");
    }

    @Test
    @DisplayName("Deve criar campanha emergencial com sucesso para receptor")
    void deveCriarCampanhaComSucesso() {
        CampanhaEmergencialRequest req = new CampanhaEmergencialRequest(
                "Campanha Chuvas de Verão",
                "Arrecadação urgente para famílias afetadas pelos alagamentos",
                enderecoReq,
                List.of(itemReq)
        );

        when(receptorRepository.findById(1L)).thenReturn(Optional.of(receptor));
        when(campanhaRepository.save(any(CampanhaEmergencial.class))).thenAnswer(invocation -> {
            CampanhaEmergencial c = invocation.getArgument(0);
            c.setId(10L);
            c.setCriadoEm(LocalDateTime.now());
            c.setAtualizadoEm(LocalDateTime.now());
            return c;
        });

        CampanhaEmergencialResponse response = service.criar(1L, req);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.nome()).isEqualTo("Campanha Chuvas de Verão");
        assertThat(response.descricao()).isEqualTo("Arrecadação urgente para famílias afetadas pelos alagamentos");
        assertThat(response.situacao()).isEqualTo(SituacaoCampanha.ATIVA);
        assertThat(response.receptor().nome()).isEqualTo("ONG Esperança");
        assertThat(response.itens()).hasSize(1);
        assertThat(response.itens().get(0).quantidade()).isEqualTo(50.0);

        verify(receptorRepository).findById(1L);
        verify(campanhaRepository).save(any(CampanhaEmergencial.class));
    }

    @Test
    @DisplayName("Deve lançar ForbiddenException se usuário não for receptor")
    void deveLancarForbiddenExceptionQuandoNaoReceptor() {
        CampanhaEmergencialRequest req = new CampanhaEmergencialRequest(
                "Campanha Inverno",
                "Descrição",
                enderecoReq,
                List.of(itemReq)
        );

        when(receptorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.criar(99L, req))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Apenas receptores podem criar campanhas");

        verify(campanhaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar campanhas do receptor autenticado")
    void deveListarMinhasCampanhas() {
        CampanhaEmergencial c1 = new CampanhaEmergencial();
        c1.setId(1L);
        c1.setNome("Campanha 1");
        c1.setReceptor(receptor);
        c1.setEnderecoRecebimento(EnderecoMapper.toEndereco(enderecoReq));
        c1.setCriadoEm(LocalDateTime.now());
        c1.setAtualizadoEm(LocalDateTime.now());

        when(campanhaRepository.findByReceptorIdOrderByCriadoEmDesc(1L)).thenReturn(List.of(c1));

        List<CampanhaEmergencialResponse> lista = service.listarMinhas(1L);

        assertThat(lista).hasSize(1);
        assertThat(lista.get(0).nome()).isEqualTo("Campanha 1");
        verify(campanhaRepository).findByReceptorIdOrderByCriadoEmDesc(1L);
    }

    @Test
    @DisplayName("Deve listar campanhas paginadas por situação")
    void deveListarPorSituacao() {
        CampanhaEmergencial c1 = new CampanhaEmergencial();
        c1.setId(1L);
        c1.setNome("Campanha Ativa");
        c1.setReceptor(receptor);
        c1.setEnderecoRecebimento(EnderecoMapper.toEndereco(enderecoReq));
        c1.setCriadoEm(LocalDateTime.now());
        c1.setAtualizadoEm(LocalDateTime.now());

        Pageable pageable = PageRequest.of(0, 10);
        Page<CampanhaEmergencial> page = new PageImpl<>(List.of(c1), pageable, 1);

        when(campanhaRepository.findBySituacao(SituacaoCampanha.ATIVA, pageable)).thenReturn(page);

        Page<CampanhaEmergencialResponse> result = service.listarPorSituacao(SituacaoCampanha.ATIVA, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).nome()).isEqualTo("Campanha Ativa");
        verify(campanhaRepository).findBySituacao(SituacaoCampanha.ATIVA, pageable);
    }

    @Test
    @DisplayName("Deve buscar campanha por ID com sucesso")
    void deveBuscarPorIdComSucesso() {
        CampanhaEmergencial c1 = new CampanhaEmergencial();
        c1.setId(5L);
        c1.setNome("Campanha Enchente");
        c1.setReceptor(receptor);
        c1.setEnderecoRecebimento(EnderecoMapper.toEndereco(enderecoReq));
        c1.setCriadoEm(LocalDateTime.now());
        c1.setAtualizadoEm(LocalDateTime.now());

        when(campanhaRepository.findWithItensById(5L)).thenReturn(Optional.of(c1));

        CampanhaEmergencialResponse resp = service.buscarPorId(5L);

        assertThat(resp).isNotNull();
        assertThat(resp.id()).isEqualTo(5L);
        assertThat(resp.nome()).isEqualTo("Campanha Enchente");
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando campanha não existir")
    void deveLancarNotFoundQuandoCampanhaNaoExistir() {
        when(campanhaRepository.findWithItensById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Campanha emergencial não encontrada");
    }
}
