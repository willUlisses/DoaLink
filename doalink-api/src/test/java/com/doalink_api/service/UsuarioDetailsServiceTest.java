package com.doalink_api.service;

import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Usuario;
import com.doalink_api.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    @DisplayName("Deve carregar UserDetails com sucesso quando usuário for encontrado pelo e-mail")
    void deveRetornarUserDetailsQuandoUsuarioExistir() {
        Usuario usuario = new Doador();
        usuario.setId(1L);
        usuario.setEmail("usuario@teste.com");
        usuario.setSenha("hashSenha123");

        when(usuarioRepository.findByEmail("usuario@teste.com")).thenReturn(Optional.of(usuario));

        UserDetails details = usuarioDetailsService.loadUserByUsername("usuario@teste.com");

        assertThat(details).isNotNull();
        assertThat(details.getUsername()).isEqualTo("usuario@teste.com");
        assertThat(details.getPassword()).isEqualTo("hashSenha123");
    }

    @Test
    @DisplayName("Deve lançar org.springframework.security.core.userdetails.UsernameNotFoundException quando usuário não for encontrado")
    void deveLancarUsernameNotFoundExceptionQuandoUsuarioNaoExistir() {
        when(usuarioRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioDetailsService.loadUserByUsername("inexistente@teste.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuário não encontrado");
    }
}
