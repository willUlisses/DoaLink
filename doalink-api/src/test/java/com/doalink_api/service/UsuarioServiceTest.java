package com.doalink_api.service;

import com.doalink_api.dtos.request.ContatoRequest;
import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.request.UsuarioAtualizarRequest;
import com.doalink_api.dtos.response.ContatoResponse;
import com.doalink_api.dtos.response.UsuarioDetalheResponse;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.entities.enums.TipoUsuario;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Doador doador;
    private Receptor receptor;

    @BeforeEach
    void setUp() {
        doador = new Doador("52998224725", "Carlos Silva", "carlos@login.com", "hash", "11999990001", TipoPessoa.FISICA);
        doador.setId(1L);
        doador.setEmailVerificado(true);

        receptor = new Receptor();
        receptor.setId(2L);
        receptor.setNome("ONG Esperança");
        receptor.setEmail("ong@login.com");
        receptor.setEmailContato("contato@esperanca.org");
        receptor.setDocumento("11222333000181");
        receptor.setTipoPessoa(TipoPessoa.JURIDICA);
        receptor.setEmailVerificado(true);
    }

    @Test
    @DisplayName("buscarPorId: deve retornar detalhes do perfil com documento, e-mail de login e e-mail de contato")
    void buscarPorId_deveRetornarDetalhesComEmailsSeparados() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(receptor));

        UsuarioDetalheResponse resp = usuarioService.buscarPorId(2L);

        assertThat(resp.id()).isEqualTo(2L);
        assertThat(resp.nome()).isEqualTo("ONG Esperança");
        assertThat(resp.documento()).isEqualTo("11222333000181");
        assertThat(resp.email()).isEqualTo("ong@login.com");
        assertThat(resp.emailContato()).isEqualTo("contato@esperanca.org");
        assertThat(resp.tipoUsuario()).isEqualTo(TipoUsuario.RECEPTOR);
    }

    @Test
    @DisplayName("atualizar: deve alterar apenas o endereço preservando nome, documento, tipo e e-mail de login")
    void atualizar_deveAtualizarSomenteEndereco() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        EnderecoRequest novoEnd = new EnderecoRequest(
                "Av. Brasil", "500", "Sala 2", "Centro", "Campinas", "SP", "13010-000");

        UsuarioDetalheResponse resp = usuarioService.atualizar(2L, new UsuarioAtualizarRequest(novoEnd));

        assertThat(resp.nome()).isEqualTo("ONG Esperança");
        assertThat(resp.documento()).isEqualTo("11222333000181");
        assertThat(resp.email()).isEqualTo("ong@login.com");
        assertThat(resp.emailContato()).isEqualTo("contato@esperanca.org");
        assertThat(resp.tipoUsuario()).isEqualTo(TipoUsuario.RECEPTOR);
        assertThat(resp.endereco()).isNotNull();
        assertThat(resp.endereco().cidade()).isEqualTo("Campinas");
        assertThat(resp.endereco().cep()).isEqualTo("13010000");
    }

    @Test
    @DisplayName("atualizarContato: deve atualizar e-mail de contato e telefone sem alterar o e-mail de login")
    void atualizarContato_deveAtualizarEmailContatoSemAlterarLogin() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(doador));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        ContatoResponse resp = usuarioService.atualizarContato(
                1L, new ContatoRequest("contato.publico@doalink.org", "(21) 97777-6666"));

        assertThat(resp.nome()).isEqualTo("Carlos Silva");
        assertThat(resp.email()).isEqualTo("contato.publico@doalink.org");
        assertThat(resp.telefone()).isEqualTo("21977776666");
        assertThat(doador.getEmail()).isEqualTo("carlos@login.com");
        assertThat(doador.getEmailContato()).isEqualTo("contato.publico@doalink.org");
    }

    @Test
    @DisplayName("atualizarContato: deve manter e-mail de contato atual quando não informado na requisição")
    void atualizarContato_semEmailInformado_deveManterEmailAtual() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(receptor));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        ContatoResponse resp = usuarioService.atualizarContato(2L, new ContatoRequest(null, "(21) 97777-6666"));

        assertThat(resp.email()).isEqualTo("contato@esperanca.org");
        assertThat(resp.telefone()).isEqualTo("21977776666");
        assertThat(receptor.getEmail()).isEqualTo("ong@login.com");
    }

    @Test
    @DisplayName("buscarPorId: deve lançar NotFoundException quando usuário não existir")
    void buscarPorId_inexistente_deveLancarNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
