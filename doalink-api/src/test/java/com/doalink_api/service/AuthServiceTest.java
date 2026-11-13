package com.doalink_api.service;

import com.doalink_api.config.UsuarioDetails;
import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.request.ReenviarCodigoRequest;
import com.doalink_api.dtos.request.VerificarEmailRequest;
import com.doalink_api.dtos.response.AuthResponse;
import com.doalink_api.dtos.response.UsuarioResponse;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Usuario;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.EmailNaoVerificadoException;
import com.doalink_api.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private VerificacaoEmailService verificacaoEmailService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Doador();
        usuario.setId(1L);
        usuario.setNome("Doador Teste");
        usuario.setEmail("doador@teste.com");
        usuario.setDocumento("52998224725");
        usuario.setEmailVerificado(false);
    }

    @Nested
    @DisplayName("Cadastro")
    class CadastroTests {

        @Test
        @DisplayName("Deve cadastrar doador, persistir com emailVerificado=false, enviar código e retornar UsuarioResponse")
        void deveCadastrarDoadorComSucesso() {
            CadastroComumRequest req = new CadastroComumRequest("52998224725", null, "doador@teste.com", "Doador Teste", "senha12345");

            when(usuarioRepository.existsByEmail("doador@teste.com")).thenReturn(false);
            when(usuarioRepository.existsByDocumento("52998224725")).thenReturn(false);
            when(passwordEncoder.encode("senha12345")).thenReturn("senhaCriptografada");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
                Usuario u = invocation.getArgument(0);
                u.setId(1L);
                return u;
            });

            UsuarioResponse resp = authService.cadastrarDoador(req);

            assertThat(resp.id()).isEqualTo(1L);
            assertThat(resp.email()).isEqualTo("doador@teste.com");
            assertThat(resp.nome()).isEqualTo("Doador Teste");

            verify(verificacaoEmailService, times(1)).enviarCodigo(any(Usuario.class));
            verifyNoInteractions(jwtService);
            verifyNoInteractions(refreshTokenService);
        }

        @Test
        @DisplayName("Deve lançar ConflictException quando e-mail já existir no cadastro")
        void deveLancarConflictQuandoEmailDuplicado() {
            CadastroComumRequest req = new CadastroComumRequest("52998224725", null, "doador@teste.com", "Doador Teste", "senha12345");

            when(usuarioRepository.existsByEmail("doador@teste.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.cadastrarDoador(req))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("E-mail já cadastrado");

            verifyNoInteractions(verificacaoEmailService);
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Deve logar com sucesso quando credenciais forem válidas e e-mail estiver verificado")
        void deveLogarComSucessoQuandoEmailVerificado() {
            usuario.setEmailVerificado(true);
            LoginRequest req = new LoginRequest("doador@teste.com", "senha12345");

            Authentication auth = mock(Authentication.class);
            UsuarioDetails details = new UsuarioDetails(usuario);
            when(auth.getPrincipal()).thenReturn(details);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
            when(jwtService.gerarAccessToken(usuario)).thenReturn("access-token-jwt");
            when(refreshTokenService.criar(usuario)).thenReturn("refresh-token-opaque");

            AuthResponse resp = authService.login(req);

            assertThat(resp.usuario().email()).isEqualTo("doador@teste.com");
            assertThat(resp.accessToken()).isEqualTo("access-token-jwt");
            assertThat(resp.refreshToken()).isEqualTo("refresh-token-opaque");
        }

        @Test
        @DisplayName("Deve lançar EmailNaoVerificadoException quando e-mail não estiver verificado")
        void deveLancarEmailNaoVerificadoException() {
            usuario.setEmailVerificado(false);
            LoginRequest req = new LoginRequest("doador@teste.com", "senha12345");

            Authentication auth = mock(Authentication.class);
            UsuarioDetails details = new UsuarioDetails(usuario);
            when(auth.getPrincipal()).thenReturn(details);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(EmailNaoVerificadoException.class)
                    .hasMessage("E-mail não verificado. Confirme o código enviado para o seu e-mail antes de acessar a conta.");

            verifyNoInteractions(jwtService);
            verifyNoInteractions(refreshTokenService);
        }

        @Test
        @DisplayName("Deve propagar BadCredentialsException quando senha estiver errada")
        void devePropagarBadCredentials() {
            LoginRequest req = new LoginRequest("doador@teste.com", "senhaErrada");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    @Nested
    @DisplayName("Verificação de E-mail")
    class VerificacaoEmailTests {

        @Test
        @DisplayName("Deve verificar código e autenticar gerando tokens")
        void deveVerificarEmailEAutenticar() {
            VerificarEmailRequest req = new VerificarEmailRequest("doador@teste.com", "123456");
            usuario.setEmailVerificado(true);

            when(verificacaoEmailService.verificarCodigo("doador@teste.com", "123456")).thenReturn(usuario);
            when(jwtService.gerarAccessToken(usuario)).thenReturn("access-token-jwt");
            when(refreshTokenService.criar(usuario)).thenReturn("refresh-token-opaque");

            AuthResponse resp = authService.verificarEmail(req);

            assertThat(resp.usuario().email()).isEqualTo("doador@teste.com");
            assertThat(resp.accessToken()).isEqualTo("access-token-jwt");
            assertThat(resp.refreshToken()).isEqualTo("refresh-token-opaque");
        }

        @Test
        @DisplayName("Deve delegar o reenvio de código para o VerificacaoEmailService")
        void deveDelegarReenvio() {
            ReenviarCodigoRequest req = new ReenviarCodigoRequest("doador@teste.com");

            authService.reenviarCodigoVerificacao(req);

            verify(verificacaoEmailService, times(1)).reenviarCodigo("doador@teste.com");
        }
    }
}
