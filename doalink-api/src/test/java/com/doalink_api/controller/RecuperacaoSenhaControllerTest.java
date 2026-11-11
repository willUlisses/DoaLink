package com.doalink_api.controller;

import com.doalink_api.dtos.request.RedefinirSenhaRequest;
import com.doalink_api.dtos.request.SolicitarRecuperacaoSenhaRequest;
import com.doalink_api.exception.GlobalExceptionHandler;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.service.RecuperacaoSenhaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecuperacaoSenhaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RecuperacaoSenhaService recuperacaoSenhaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new RecuperacaoSenhaController(recuperacaoSenhaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // -------------------------------------------------------------------------
    // POST /auth/recuperar-senha
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve solicitar recuperação de senha com e-mail válido e retornar 200 OK")
    void deveSolicitarRecuperacaoComSucesso() throws Exception {
        SolicitarRecuperacaoSenhaRequest req = new SolicitarRecuperacaoSenhaRequest("usuario@teste.com");

        doNothing().when(recuperacaoSenhaService).solicitarRecuperacao("usuario@teste.com");

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Se o e-mail informado estiver cadastrado, você receberá um link para redefinir sua senha em breve."));

        verify(recuperacaoSenhaService, times(1)).solicitarRecuperacao("usuario@teste.com");
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao solicitar recuperação com e-mail inválido")
    void deveRetornar400AoSoliciarComEmailInvalido() throws Exception {
        SolicitarRecuperacaoSenhaRequest req = new SolicitarRecuperacaoSenhaRequest("email-invalido");

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(recuperacaoSenhaService, never()).solicitarRecuperacao(any());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request ao solicitar recuperação com e-mail em branco")
    void deveRetornar400AoSolicitarComEmailVazio() throws Exception {
        SolicitarRecuperacaoSenhaRequest req = new SolicitarRecuperacaoSenhaRequest("");

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(recuperacaoSenhaService, never()).solicitarRecuperacao(any());
    }

    // -------------------------------------------------------------------------
    // GET /auth/recuperar-senha/validar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve validar token existente e retornar 200 OK com valido=true")
    void deveRetornarValidoTrueParaTokenValido() throws Exception {
        when(recuperacaoSenhaService.validarToken("token-valido-123")).thenReturn(true);

        mockMvc.perform(get("/auth/recuperar-senha/validar")
                        .param("token", "token-valido-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));

        verify(recuperacaoSenhaService, times(1)).validarToken("token-valido-123");
    }

    @Test
    @DisplayName("Deve validar token inexistente ou expirado e retornar 200 OK com valido=false")
    void deveRetornarValidoFalseParaTokenInvalido() throws Exception {
        when(recuperacaoSenhaService.validarToken("token-invalido-xyz")).thenReturn(false);

        mockMvc.perform(get("/auth/recuperar-senha/validar")
                        .param("token", "token-invalido-xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));

        verify(recuperacaoSenhaService, times(1)).validarToken("token-invalido-xyz");
    }

    // -------------------------------------------------------------------------
    // POST /auth/redefinir-senha
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Deve redefinir senha com sucesso retornando 200 OK")
    void deveRedefinirSenhaComSucesso() throws Exception {
        RedefinirSenhaRequest req = new RedefinirSenhaRequest("token-valido", "NovaSenha123@");

        doNothing().when(recuperacaoSenhaService).redefinirSenha("token-valido", "NovaSenha123@");

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Senha redefinida com sucesso."));

        verify(recuperacaoSenhaService, times(1)).redefinirSenha("token-valido", "NovaSenha123@");
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando token for inválido, expirado ou já utilizado")
    void deveRetornar404QuandoTokenInvalidoOuExpirado() throws Exception {
        RedefinirSenhaRequest req = new RedefinirSenhaRequest("token-expirado", "NovaSenha123@");

        doThrow(new NotFoundException("Token de recuperação inválido ou expirado"))
                .when(recuperacaoSenhaService).redefinirSenha("token-expirado", "NovaSenha123@");

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Token de recuperação inválido ou expirado"));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando senha tiver menos de 8 caracteres")
    void deveRetornar400QuandoSenhaCurta() throws Exception {
        RedefinirSenhaRequest req = new RedefinirSenhaRequest("token-valido", "1234567");

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(recuperacaoSenhaService, never()).redefinirSenha(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando senha exceder 72 caracteres")
    void deveRetornar400QuandoSenhaLonga() throws Exception {
        String senhaLonga = "A".repeat(73);
        RedefinirSenhaRequest req = new RedefinirSenhaRequest("token-valido", senhaLonga);

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(recuperacaoSenhaService, never()).redefinirSenha(any(), any());
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando token for em branco")
    void deveRetornar400QuandoTokenEmBranco() throws Exception {
        RedefinirSenhaRequest req = new RedefinirSenhaRequest("", "NovaSenha123@");

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(recuperacaoSenhaService, never()).redefinirSenha(any(), any());
    }
}
