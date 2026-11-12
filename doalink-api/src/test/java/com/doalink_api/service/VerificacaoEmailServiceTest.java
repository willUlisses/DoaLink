package com.doalink_api.service;

import com.doalink_api.entities.CodigoVerificacaoEmail;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Usuario;
import com.doalink_api.exception.CodigoInvalidoException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.CodigoVerificacaoEmailRepository;
import com.doalink_api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificacaoEmailServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CodigoVerificacaoEmailRepository codigoRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificacaoEmailService verificacaoEmailService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(verificacaoEmailService, "expiracaoSegundos", 90L);

        usuario = new Doador();
        usuario.setId(10L);
        usuario.setNome("Doador Teste");
        usuario.setEmail("doador@teste.com");
        usuario.setEmailVerificado(false);
    }

    private String calcularHash(String codigo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(codigo.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    @DisplayName("enviarCodigo()")
    class EnviarCodigoTests {

        @Test
        @DisplayName("Deve revogar pendentes anteriores, salvar novo código e disparar e-mail")
        void deveEnviarCodigoComSucesso() {
            verificacaoEmailService.enviarCodigo(usuario);

            verify(codigoRepository, times(1)).revogarPendentesDoUsuario(10L);

            ArgumentCaptor<CodigoVerificacaoEmail> captor = ArgumentCaptor.forClass(CodigoVerificacaoEmail.class);
            verify(codigoRepository, times(1)).save(captor.capture());

            CodigoVerificacaoEmail salvo = captor.getValue();
            assertThat(salvo.getUsuario()).isEqualTo(usuario);
            assertThat(salvo.getCodigoHash()).hasSize(64);
            assertThat(salvo.isUtilizado()).isFalse();
            assertThat(salvo.isRevogado()).isFalse();
            assertThat(salvo.getExpiracao()).isAfter(Instant.now());

            verify(emailService, times(1)).enviarCodigoVerificacao(eq("doador@teste.com"), eq("Doador Teste"), anyString());
        }
    }

    @Nested
    @DisplayName("verificarCodigo()")
    class VerificarCodigoTests {

        @Test
        @DisplayName("Deve verificar código válido com sucesso, marcando utilizado e emailVerificado = true")
        void deveVerificarCodigoComSucesso() {
            String codigo = "123456";
            String hash = calcularHash(codigo);
            Instant expiracao = Instant.now().plusSeconds(90);
            CodigoVerificacaoEmail entity = new CodigoVerificacaoEmail(usuario, hash, expiracao);

            when(usuarioRepository.findByEmail("doador@teste.com")).thenReturn(Optional.of(usuario));
            when(codigoRepository.findFirstByUsuarioIdAndRevogadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(10L))
                    .thenReturn(Optional.of(entity));

            Usuario resultado = verificacaoEmailService.verificarCodigo("doador@teste.com", codigo);

            assertThat(resultado.isEmailVerificado()).isTrue();
            assertThat(entity.isUtilizado()).isTrue();
            verify(codigoRepository, times(1)).save(entity);
            verify(usuarioRepository, times(1)).save(usuario);
            verify(codigoRepository, times(1)).deletarExpirados(any());
        }

        @Test
        @DisplayName("Deve retornar usuário diretamente se e-mail já estiver verificado")
        void deveRetornarDiretamenteSeJaVerificado() {
            usuario.setEmailVerificado(true);
            when(usuarioRepository.findByEmail("doador@teste.com")).thenReturn(Optional.of(usuario));

            Usuario resultado = verificacaoEmailService.verificarCodigo("doador@teste.com", "123456");

            assertThat(resultado.isEmailVerificado()).isTrue();
            verifyNoInteractions(codigoRepository);
        }

        @Test
        @DisplayName("Deve lançar NotFoundException se usuário não existir")
        void deveLancarNotFoundQuandoUsuarioInexistente() {
            when(usuarioRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> verificacaoEmailService.verificarCodigo("inexistente@teste.com", "123456"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Usuário não encontrado");
        }

        @Test
        @DisplayName("Deve lançar CodigoInvalidoException se não houver código pendente")
        void deveLancarExcecaoQuandoNenhumCodigoPendente() {
            when(usuarioRepository.findByEmail("doador@teste.com")).thenReturn(Optional.of(usuario));
            when(codigoRepository.findFirstByUsuarioIdAndRevogadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(10L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> verificacaoEmailService.verificarCodigo("doador@teste.com", "123456"))
                    .isInstanceOf(CodigoInvalidoException.class)
                    .hasMessage("Código de verificação inválido ou inexistente");
        }

        @Test
        @DisplayName("Deve revogar e lançar CodigoInvalidoException se código estiver expirado")
        void deveLancarExcecaoQuandoCodigoExpirado() {
            String codigo = "123456";
            String hash = calcularHash(codigo);
            Instant expirado = Instant.now().minusSeconds(10);
            CodigoVerificacaoEmail entity = new CodigoVerificacaoEmail(usuario, hash, expirado);

            when(usuarioRepository.findByEmail("doador@teste.com")).thenReturn(Optional.of(usuario));
            when(codigoRepository.findFirstByUsuarioIdAndRevogadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(10L))
                    .thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> verificacaoEmailService.verificarCodigo("doador@teste.com", codigo))
                    .isInstanceOf(CodigoInvalidoException.class)
                    .hasMessage("Código de verificação expirado. Solicite um novo código.");

            assertThat(entity.isRevogado()).isTrue();
            verify(codigoRepository, times(1)).save(entity);
            assertThat(usuario.isEmailVerificado()).isFalse();
        }

        @Test
        @DisplayName("Deve lançar CodigoInvalidoException se código digitado for incorreto")
        void deveLancarExcecaoQuandoCodigoIncorreto() {
            String hash = calcularHash("123456");
            Instant expiracao = Instant.now().plusSeconds(90);
            CodigoVerificacaoEmail entity = new CodigoVerificacaoEmail(usuario, hash, expiracao);

            when(usuarioRepository.findByEmail("doador@teste.com")).thenReturn(Optional.of(usuario));
            when(codigoRepository.findFirstByUsuarioIdAndRevogadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(10L))
                    .thenReturn(Optional.of(entity));

            assertThatThrownBy(() -> verificacaoEmailService.verificarCodigo("doador@teste.com", "654321"))
                    .isInstanceOf(CodigoInvalidoException.class)
                    .hasMessage("Código de verificação incorreto");

            assertThat(entity.isUtilizado()).isFalse();
            assertThat(usuario.isEmailVerificado()).isFalse();
        }
    }

    @Nested
    @DisplayName("reenviarCodigo()")
    class ReenviarCodigoTests {

        @Test
        @DisplayName("Deve reenviar código para usuário cadastrado e não verificado")
        void deveReenviarCodigoComSucesso() {
            when(usuarioRepository.findByEmail("doador@teste.com")).thenReturn(Optional.of(usuario));

            verificacaoEmailService.reenviarCodigo("doador@teste.com");

            verify(codigoRepository, times(1)).revogarPendentesDoUsuario(10L);
            verify(codigoRepository, times(1)).save(any(CodigoVerificacaoEmail.class));
            verify(emailService, times(1)).enviarCodigoVerificacao(eq("doador@teste.com"), anyString(), anyString());
        }

        @Test
        @DisplayName("Não deve reenviar e deve ignorar silenciosamente se usuário não existir")
        void deveIgnorarSeUsuarioNaoExistir() {
            when(usuarioRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

            verificacaoEmailService.reenviarCodigo("inexistente@teste.com");

            verifyNoInteractions(codigoRepository);
            verifyNoInteractions(emailService);
        }

        @Test
        @DisplayName("Não deve reenviar se usuário já estiver com e-mail verificado")
        void deveIgnorarSeJaVerificado() {
            usuario.setEmailVerificado(true);
            when(usuarioRepository.findByEmail("doador@teste.com")).thenReturn(Optional.of(usuario));

            verificacaoEmailService.reenviarCodigo("doador@teste.com");

            verifyNoInteractions(codigoRepository);
            verifyNoInteractions(emailService);
        }
    }
}
