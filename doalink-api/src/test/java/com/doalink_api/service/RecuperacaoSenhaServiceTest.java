package com.doalink_api.service;

import com.doalink_api.entities.Doador;
import com.doalink_api.entities.TokenRecuperacaoSenha;
import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.RefreshTokenRepository;
import com.doalink_api.repository.TokenRecuperacaoSenhaRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private TokenRecuperacaoSenhaRepository tokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private RecuperacaoSenhaService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "expiracaoMinutos", 10L);

        usuario = new Doador("12345678901", "Maria Silva", "maria@teste.com", "hashSenhaAntiga", "11999999999", TipoPessoa.FISICA);
        usuario.setId(10L);
    }

    private String calcularHashSha256(String token) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    @Nested
    @DisplayName("solicitarRecuperacao()")
    class SolicitarRecuperacaoTests {

        @Test
        @DisplayName("Deve gerar token, persistir hash e enviar e-mail quando usuário existe")
        void deveSolicitarComSucessoQuandoUsuarioExiste() {
            when(usuarioRepository.findByEmail("maria@teste.com")).thenReturn(Optional.of(usuario));

            service.solicitarRecuperacao("  MARIA@TESTE.COM ");

            ArgumentCaptor<TokenRecuperacaoSenha> tokenCaptor = ArgumentCaptor.forClass(TokenRecuperacaoSenha.class);
            verify(tokenRepository, times(1)).save(tokenCaptor.capture());

            TokenRecuperacaoSenha tokenSalvo = tokenCaptor.getValue();
            assertThat(tokenSalvo.getUsuario()).isEqualTo(usuario);
            assertThat(tokenSalvo.getTokenHash()).isNotBlank();
            assertThat(tokenSalvo.isUtilizado()).isFalse();
            assertThat(tokenSalvo.getExpiracao()).isAfter(Instant.now());

            ArgumentCaptor<String> tokenOpacoCaptor = ArgumentCaptor.forClass(String.class);
            verify(emailService, times(1)).enviarRecuperacaoSenha(
                    eq("maria@teste.com"),
                    eq("Maria Silva"),
                    tokenOpacoCaptor.capture()
            );

            assertThat(tokenOpacoCaptor.getValue()).isNotBlank();
        }

        @Test
        @DisplayName("Não deve gerar token nem enviar e-mail quando usuário não existe (resposta uniforme)")
        void naoDeveFazerNadaQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findByEmail("inexistente@teste.com")).thenReturn(Optional.empty());

            service.solicitarRecuperacao("inexistente@teste.com");

            verify(tokenRepository, never()).save(any());
            verify(emailService, never()).enviarRecuperacaoSenha(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("validarToken()")
    class ValidarTokenTests {

        @Test
        @DisplayName("Deve retornar true quando token existe, não expirou e não foi utilizado")
        void deveRetornarTrueQuandoTokenValido() throws Exception {
            String tokenOpaco = "token-valido-123";
            String tokenHash = calcularHashSha256(tokenOpaco);
            Instant expiracaoFutura = Instant.now().plus(10, ChronoUnit.MINUTES);

            TokenRecuperacaoSenha tokenEntity = new TokenRecuperacaoSenha(usuario, tokenHash, expiracaoFutura);

            when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(tokenEntity));

            boolean valido = service.validarToken(tokenOpaco);

            assertThat(valido).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando token não existe")
        void deveRetornarFalseQuandoTokenNaoExiste() throws Exception {
            String tokenOpaco = "token-inexistente";
            String tokenHash = calcularHashSha256(tokenOpaco);

            when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

            boolean valido = service.validarToken(tokenOpaco);

            assertThat(valido).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando token está expirado")
        void deveRetornarFalseQuandoTokenExpirado() throws Exception {
            String tokenOpaco = "token-expirado";
            String tokenHash = calcularHashSha256(tokenOpaco);
            Instant expiracaoPassada = Instant.now().minus(5, ChronoUnit.MINUTES);

            TokenRecuperacaoSenha tokenEntity = new TokenRecuperacaoSenha(usuario, tokenHash, expiracaoPassada);

            when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(tokenEntity));

            boolean valido = service.validarToken(tokenOpaco);

            assertThat(valido).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando token já foi utilizado")
        void deveRetornarFalseQuandoTokenJaUtilizado() throws Exception {
            String tokenOpaco = "token-ja-usado";
            String tokenHash = calcularHashSha256(tokenOpaco);
            Instant expiracaoFutura = Instant.now().plus(10, ChronoUnit.MINUTES);

            TokenRecuperacaoSenha tokenEntity = new TokenRecuperacaoSenha(usuario, tokenHash, expiracaoFutura);
            tokenEntity.setUtilizado(true);

            when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(tokenEntity));

            boolean valido = service.validarToken(tokenOpaco);

            assertThat(valido).isFalse();
        }
    }

    @Nested
    @DisplayName("redefinirSenha()")
    class RedefinirSenhaTests {

        @Test
        @DisplayName("Deve atualizar a senha, revogar refresh tokens e marcar token como utilizado com sucesso")
        void deveRedefinirSenhaComSucesso() throws Exception {
            String tokenOpaco = "token-valido-abc";
            String tokenHash = calcularHashSha256(tokenOpaco);
            Instant expiracaoFutura = Instant.now().plus(10, ChronoUnit.MINUTES);

            TokenRecuperacaoSenha tokenEntity = new TokenRecuperacaoSenha(usuario, tokenHash, expiracaoFutura);

            when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(tokenEntity));
            when(passwordEncoder.encode("NovaSenha@2026")).thenReturn("novaSenhaCriptografada");

            service.redefinirSenha(tokenOpaco, "NovaSenha@2026");

            assertThat(usuario.getSenha()).isEqualTo("novaSenhaCriptografada");
            assertThat(tokenEntity.isUtilizado()).isTrue();

            verify(refreshTokenRepository, times(1)).revogarTodosDoUsuario(10L);
            verify(tokenRepository, times(1)).deletarExpirados(any(Instant.class));
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando token não existe")
        void deveLancarNotFoundQuandoTokenNaoExiste() throws Exception {
            String tokenOpaco = "token-inexistente";
            String tokenHash = calcularHashSha256(tokenOpaco);

            when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.redefinirSenha(tokenOpaco, "NovaSenha@2026"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Token de recuperação inválido ou expirado");

            verify(passwordEncoder, never()).encode(any());
            verify(refreshTokenRepository, never()).revogarTodosDoUsuario(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando token está expirado")
        void deveLancarNotFoundQuandoTokenExpirado() throws Exception {
            String tokenOpaco = "token-expirado";
            String tokenHash = calcularHashSha256(tokenOpaco);
            Instant expiracaoPassada = Instant.now().minus(1, ChronoUnit.MINUTES);

            TokenRecuperacaoSenha tokenEntity = new TokenRecuperacaoSenha(usuario, tokenHash, expiracaoPassada);

            when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(tokenEntity));

            assertThatThrownBy(() -> service.redefinirSenha(tokenOpaco, "NovaSenha@2026"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Token de recuperação inválido ou expirado");

            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        @DisplayName("Deve lançar NotFoundException quando token já foi utilizado")
        void deveLancarNotFoundQuandoTokenJaUtilizado() throws Exception {
            String tokenOpaco = "token-ja-usado";
            String tokenHash = calcularHashSha256(tokenOpaco);
            Instant expiracaoFutura = Instant.now().plus(10, ChronoUnit.MINUTES);

            TokenRecuperacaoSenha tokenEntity = new TokenRecuperacaoSenha(usuario, tokenHash, expiracaoFutura);
            tokenEntity.setUtilizado(true);

            when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(tokenEntity));

            assertThatThrownBy(() -> service.redefinirSenha(tokenOpaco, "NovaSenha@2026"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Token de recuperação inválido ou expirado");

            verify(passwordEncoder, never()).encode(any());
        }
    }
}
