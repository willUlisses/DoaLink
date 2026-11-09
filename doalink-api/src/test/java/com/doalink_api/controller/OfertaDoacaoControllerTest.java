package com.doalink_api.controller;

import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.request.ItemDoacaoRequest;
import com.doalink_api.dtos.request.OfertaDoacaoRequest;
import com.doalink_api.dtos.response.*;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.GlobalExceptionHandler;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.service.OfertaDoacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OfertaDoacaoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OfertaDoacaoService ofertaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final UsuarioAutenticado DOADOR_PRINCIPAL = new UsuarioAutenticado(1L, "doador@email.com");

    /**
     * RequestPostProcessor que injeta o Authentication diretamente no SecurityContextHolder
     * da thread do MockMvc, contornando a limitação do ThreadLocal no standaloneSetup.
     */
    private static final RequestPostProcessor AS_DOADOR = request -> {
        var auth = new UsernamePasswordAuthenticationToken(
                DOADOR_PRINCIPAL, null,
                List.of(new SimpleGrantedAuthority("ROLE_DOADOR")));
        var ctx = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(ctx);
        request.setAttribute("SPRING_SECURITY_CONTEXT", ctx);
        return request;
    };

    @BeforeEach
    void setUp() {
        // InheritableThreadLocal garante que o SecurityContext seja visível
        // na thread do dispatcher do MockMvc (que é uma thread filha)
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new OfertaDoacaoController(ofertaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private OfertaDoacaoResponse responseExemplo() {
        ItemDoacaoResponse item = new ItemDoacaoResponse(
                1L, "Arroz tipo 1", TipoItemDoacao.NAO_PERECIVEL, BigDecimal.valueOf(5));
        EnderecoResponse endereco = new EnderecoResponse(
                "Rua das Flores", "10", null, "Centro", "São Paulo", "SP", "01310100");
        ContatoResponse contato = new ContatoResponse("João Silva", "joao@email.com", "11999999999");
        return new OfertaDoacaoResponse(
                10L, SituacaoOferta.DISPONIVEL, contato, endereco,
                List.of(item), LocalDateTime.now(), LocalDateTime.now());
    }

    private OfertaDoacaoRequest reqValida() {
        ItemDoacaoRequest item = new ItemDoacaoRequest(
                "Arroz tipo 1", TipoItemDoacao.NAO_PERECIVEL, BigDecimal.valueOf(5));
        EnderecoRequest endereco = new EnderecoRequest(
                "Rua das Flores", "10", null, "Centro", "São Paulo", "SP", "01310-100");
        return new OfertaDoacaoRequest(List.of(item), endereco);
    }

    // -------------------------------------------------------------------------
    // POST /ofertas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("POST /ofertas: doador autenticado → 201 + Location")
    void criar_deveRetornar201() throws Exception {
        when(ofertaService.criar(eq(1L), any())).thenReturn(responseExemplo());

        mockMvc.perform(post("/ofertas")
                        .with(AS_DOADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqValida())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/ofertas/10")))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"));
    }

    @Test
    @DisplayName("POST /ofertas: lista vazia → 400")
    void criar_listaVazia_deveRetornar400() throws Exception {
        OfertaDoacaoRequest reqInvalida = new OfertaDoacaoRequest(List.of(), null);

        mockMvc.perform(post("/ofertas")
                        .with(AS_DOADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqInvalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /ofertas: quantidade ≤ 0 → 400")
    void criar_quantidadeZero_deveRetornar400() throws Exception {
        ItemDoacaoRequest itemInvalido = new ItemDoacaoRequest(
                "Arroz", TipoItemDoacao.NAO_PERECIVEL, BigDecimal.ZERO);

        mockMvc.perform(post("/ofertas")
                        .with(AS_DOADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OfertaDoacaoRequest(List.of(itemInvalido), null))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /ofertas: tipoItemDoacao nulo → 400")
    void criar_categoriaNula_deveRetornar400() throws Exception {
        String body = """
                {"itens":[{"descricao":"Arroz","quantidade":5}],"endereco":null}
                """;

        mockMvc.perform(post("/ofertas")
                        .with(AS_DOADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /ofertas: descricao em branco → 400")
    void criar_descricaoEmBranco_deveRetornar400() throws Exception {
        ItemDoacaoRequest itemInvalido = new ItemDoacaoRequest(
                "   ", TipoItemDoacao.NAO_PERECIVEL, BigDecimal.valueOf(5));

        mockMvc.perform(post("/ofertas")
                        .with(AS_DOADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OfertaDoacaoRequest(List.of(itemInvalido), null))))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /ofertas/minhas
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /ofertas/minhas: doador autenticado → 200 com lista")
    void listarMinhas_deveRetornar200() throws Exception {
        when(ofertaService.listarMinhas(1L)).thenReturn(List.of(responseExemplo()));

        mockMvc.perform(get("/ofertas/minhas").with(AS_DOADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    // -------------------------------------------------------------------------
    // GET /ofertas/{id}
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GET /ofertas/{id}: oferta existente → 200")
    void buscarPorId_deveRetornar200() throws Exception {
        when(ofertaService.buscarPorId(10L)).thenReturn(responseExemplo());

        mockMvc.perform(get("/ofertas/10").with(AS_DOADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    @DisplayName("GET /ofertas/{id}: oferta inexistente → 404")
    void buscarPorId_deveRetornar404() throws Exception {
        when(ofertaService.buscarPorId(999L))
                .thenThrow(new NotFoundException("Oferta não encontrada: 999"));

        mockMvc.perform(get("/ofertas/999").with(AS_DOADOR))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // -------------------------------------------------------------------------
    // PATCH /ofertas/{id}/cancelar
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("PATCH /ofertas/{id}/cancelar: dono cancela → 204")
    void cancelar_deveRetornar204() throws Exception {
        doNothing().when(ofertaService).cancelar(1L, 10L);

        mockMvc.perform(patch("/ofertas/10/cancelar").with(AS_DOADOR))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /ofertas/{id}/cancelar: outro doador → 403")
    void cancelar_outroDoador_deveRetornar403() throws Exception {
        doThrow(new ForbiddenException("Você não tem permissão para cancelar esta oferta"))
                .when(ofertaService).cancelar(1L, 10L);

        mockMvc.perform(patch("/ofertas/10/cancelar").with(AS_DOADOR))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("PATCH /ofertas/{id}/cancelar: oferta inexistente → 404")
    void cancelar_ofertaInexistente_deveRetornar404() throws Exception {
        doThrow(new NotFoundException("Oferta não encontrada: 999"))
                .when(ofertaService).cancelar(1L, 999L);

        mockMvc.perform(patch("/ofertas/999/cancelar").with(AS_DOADOR))
                .andExpect(status().isNotFound());
    }
}
