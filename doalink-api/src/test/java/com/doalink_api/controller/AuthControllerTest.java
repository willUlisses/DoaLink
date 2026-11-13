package com.doalink_api.controller;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.request.ReenviarCodigoRequest;
import com.doalink_api.dtos.request.VerificarEmailRequest;
import com.doalink_api.dtos.response.AuthResponse;
import com.doalink_api.dtos.response.UsuarioResponse;
import com.doalink_api.entities.enums.TipoUsuario;
import com.doalink_api.exception.EmailNaoVerificadoException;
import com.doalink_api.exception.GlobalExceptionHandler;
import com.doalink_api.service.AuthService;
import com.doalink_api.service.CookieService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private CookieService cookieService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService, cookieService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Deve realizar login com credenciais válidas e retornar 200 OK")
    void deveRealizarLoginComSucesso() throws Exception {
        LoginRequest req = new LoginRequest("usuario@teste.com", "senhaForte123");
        UsuarioResponse userResp = new UsuarioResponse(1L, "Usuário Teste", "usuario@teste.com", TipoUsuario.DOADOR);
        AuthResponse authResp = new AuthResponse(userResp, "access-token-jwt", "refresh-token-opaque");

        when(authService.login(any(LoginRequest.class))).thenReturn(authResp);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("usuario@teste.com"))
                .andExpect(jsonPath("$.nome").value("Usuário Teste"))
                .andExpect(jsonPath("$.tipoUsuario").value("DOADOR"));

        verify(cookieService, times(1)).adicionarHeaders(any(), eq("access-token-jwt"), eq("refresh-token-opaque"));
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden ao tentar login com e-mail não verificado")
    void deveRetornar403QuandoEmailNaoVerificadoNoLogin() throws Exception {
        LoginRequest req = new LoginRequest("usuario@teste.com", "senhaForte123");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new EmailNaoVerificadoException("E-mail não verificado. Confirme o código enviado para o seu e-mail antes de acessar a conta."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("E-mail não verificado. Confirme o código enviado para o seu e-mail antes de acessar a conta."));

        verify(cookieService, never()).adicionarHeaders(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cadastrar doador com sucesso retornando 201 Created sem cookies de sessão")
    void deveCadastrarDoadorComSucessoSemCookies() throws Exception {
        CadastroComumRequest req = new CadastroComumRequest("52998224725", null, "usuario@teste.com", "Usuário Teste", "senhaForte123");
        UsuarioResponse userResp = new UsuarioResponse(1L, "Usuário Teste", "usuario@teste.com", TipoUsuario.DOADOR);

        when(authService.cadastrarDoador(any(CadastroComumRequest.class))).thenReturn(userResp);

        mockMvc.perform(post("/auth/doadores/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("usuario@teste.com"));

        verify(cookieService, never()).adicionarHeaders(any(), any(), any());
    }

    @Test
    @DisplayName("Deve verificar e-mail com sucesso e emitir cookies de autenticação")
    void deveVerificarEmailComSucesso() throws Exception {
        VerificarEmailRequest req = new VerificarEmailRequest("usuario@teste.com", "123456");
        UsuarioResponse userResp = new UsuarioResponse(1L, "Usuário Teste", "usuario@teste.com", TipoUsuario.DOADOR);
        AuthResponse authResp = new AuthResponse(userResp, "access-token-jwt", "refresh-token-opaque");

        when(authService.verificarEmail(any(VerificarEmailRequest.class))).thenReturn(authResp);

        mockMvc.perform(post("/auth/verificar-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("usuario@teste.com"));

        verify(cookieService, times(1)).adicionarHeaders(any(), eq("access-token-jwt"), eq("refresh-token-opaque"));
    }

    @Test
    @DisplayName("Deve reenviar código de confirmação com sucesso retornando 200 OK")
    void deveReenviarCodigoComSucesso() throws Exception {
        ReenviarCodigoRequest req = new ReenviarCodigoRequest("usuario@teste.com");

        doNothing().when(authService).reenviarCodigoVerificacao(any(ReenviarCodigoRequest.class));

        mockMvc.perform(post("/auth/reenviar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Se o e-mail estiver cadastrado e pendente de verificação, um novo código foi enviado."));

        verify(authService, times(1)).reenviarCodigoVerificacao(any(ReenviarCodigoRequest.class));
    }

    @Test
    @DisplayName("Deve rejeitar login com credenciais inválidas retornando 401 Unauthorized")
    void deveRetornar401QuandoCredenciaisInvalidas() throws Exception {
        LoginRequest req = new LoginRequest("usuario@teste.com", "senhaIncorreta");

        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Credenciais inválidas"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos"));
    }

    @Test
    @DisplayName("Deve rejeitar requisição de cadastro com dados inválidos retornando 400 Bad Request")
    void deveRetornar400QuandoDadosInvalidos() throws Exception {
        String bodyInvalido = "{\"email\":\"email-invalido\",\"nome\":\"\"}";

        mockMvc.perform(post("/auth/doadores/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Deve realizar refresh do token com cookie presente retornando 200 OK")
    void deveRealizarRefreshComSucesso() throws Exception {
        UsuarioResponse userResp = new UsuarioResponse(1L, "Usuário Teste", "usuario@teste.com", TipoUsuario.DOADOR);
        AuthResponse authResp = new AuthResponse(userResp, "novo-access-token", "novo-refresh-token");

        when(authService.refresh("token-antigo")).thenReturn(authResp);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refresh_token", "token-antigo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("usuario@teste.com"));

        verify(cookieService, times(1)).adicionarHeaders(any(), eq("novo-access-token"), eq("novo-refresh-token"));
    }

    @Test
    @DisplayName("Deve realizar logout e retornar 204 No Content limpando os cookies")
    void deveRealizarLogoutComSucesso() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refresh_token", "meu-refresh-token")))
                .andExpect(status().isNoContent());

        verify(authService, times(1)).logout("meu-refresh-token");
        verify(cookieService, times(1)).limpar(any());
    }
}
