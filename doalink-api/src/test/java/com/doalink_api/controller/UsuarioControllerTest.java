package com.doalink_api.controller;

import com.doalink_api.dtos.request.ContatoRequest;
import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.request.UsuarioAtualizarRequest;
import com.doalink_api.dtos.response.ContatoResponse;
import com.doalink_api.dtos.response.EnderecoResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.dtos.response.UsuarioDetalheResponse;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.entities.enums.TipoUsuario;
import com.doalink_api.exception.GlobalExceptionHandler;
import com.doalink_api.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final RequestPostProcessor AS_RECEPTOR = req -> {
        var auth = new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(2L, "ong@login.com"), null,
                List.of(new SimpleGrantedAuthority("ROLE_RECEPTOR")));
        var ctx = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(ctx);
        req.setAttribute("SPRING_SECURITY_CONTEXT", ctx);
        return req;
    };

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UsuarioController(usuarioService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private UsuarioDetalheResponse exemploReceptor() {
        return new UsuarioDetalheResponse(
                2L,
                "ONG Esperança",
                "11222333000181",
                "ong@login.com",
                "contato@esperanca.org",
                "11999990002",
                TipoUsuario.RECEPTOR,
                TipoPessoa.JURIDICA,
                true,
                new EnderecoResponse("Av. Paulista", "100", null, "Bela Vista", "São Paulo", "SP", "01310100")
        );
    }

    @Test
    @DisplayName("GET /usuarios/me: deve retornar perfil completo do usuário autenticado")
    void obterMeuPerfil_deveRetornar200() throws Exception {
        when(usuarioService.buscarPorId(2L)).thenReturn(exemploReceptor());

        mockMvc.perform(get("/usuarios/me").with(AS_RECEPTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("ONG Esperança"))
                .andExpect(jsonPath("$.documento").value("11222333000181"))
                .andExpect(jsonPath("$.email").value("ong@login.com"))
                .andExpect(jsonPath("$.emailContato").value("contato@esperanca.org"));
    }

    @Test
    @DisplayName("PUT /usuarios/me: deve atualizar apenas endereço do usuário autenticado")
    void atualizarMeuPerfil_deveRetornar200() throws Exception {
        EnderecoRequest end = new EnderecoRequest(
                "Av. Paulista", "100", null, "Bela Vista", "São Paulo", "SP", "01310-100");
        when(usuarioService.atualizar(eq(2L), any())).thenReturn(exemploReceptor());

        mockMvc.perform(put("/usuarios/me")
                        .with(AS_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UsuarioAtualizarRequest(end))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("GET/PUT/PATCH /usuarios/me/contato: deve retornar e atualizar opções de contato")
    void contato_deveRetornarEAtualizar() throws Exception {
        ContatoResponse resp = new ContatoResponse("ONG Esperança", "contato@esperanca.org", "11999990002");
        when(usuarioService.obterContato(2L)).thenReturn(resp);
        when(usuarioService.atualizarContato(eq(2L), any())).thenReturn(resp);

        mockMvc.perform(get("/usuarios/me/contato").with(AS_RECEPTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("contato@esperanca.org"))
                .andExpect(jsonPath("$.telefone").value("11999990002"));

        mockMvc.perform(put("/usuarios/me/contato")
                        .with(AS_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ContatoRequest("contato@esperanca.org", "11999990002"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("11999990002"));

        mockMvc.perform(patch("/usuarios/me/contato")
                        .with(AS_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ContatoRequest("contato@esperanca.org", "11999990002"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("contato@esperanca.org"));
    }

    @Test
    @DisplayName("Endpoints globais removidos (/usuarios) devem retornar 404/405 no controller")
    void endpointsGlobaisRemovidos_naoDevemExistir() throws Exception {
        mockMvc.perform(get("/usuarios").with(AS_RECEPTOR))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/usuarios/2").with(AS_RECEPTOR))
                .andExpect(status().is4xxClientError());
    }
}
