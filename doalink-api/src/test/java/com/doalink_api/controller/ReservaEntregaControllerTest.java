package com.doalink_api.controller;

import com.doalink_api.dtos.request.ReservaEntregaRequest;
import com.doalink_api.dtos.request.VincularTransporteRequest;
import com.doalink_api.dtos.response.*;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.GlobalExceptionHandler;
import com.doalink_api.service.ReservaEntregaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReservaEntregaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReservaEntregaService reservaService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final RequestPostProcessor AS_RECEPTOR = req -> {
        var auth = new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(2L, "ong@email.com"), null,
                List.of(new SimpleGrantedAuthority("ROLE_RECEPTOR")));
        var ctx = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(ctx);
        req.setAttribute("SPRING_SECURITY_CONTEXT", ctx);
        return req;
    };

    private static final RequestPostProcessor AS_TRANSPORTADOR = req -> {
        var auth = new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(3L, "carlos@email.com"), null,
                List.of(new SimpleGrantedAuthority("ROLE_TRANSPORTADOR")));
        var ctx = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(ctx);
        req.setAttribute("SPRING_SECURITY_CONTEXT", ctx);
        return req;
    };

    private static final RequestPostProcessor AS_DOADOR = req -> {
        var auth = new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(1L, "doador@email.com"), null,
                List.of(new SimpleGrantedAuthority("ROLE_DOADOR")));
        var ctx = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(ctx);
        req.setAttribute("SPRING_SECURITY_CONTEXT", ctx);
        return req;
    };

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ReservaEntregaController(reservaService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private ReservaEntregaResponse responseExemplo(SituacaoReservaEntrega situacao, Long dispId) {
        return new ReservaEntregaResponse(
                50L,
                situacao,
                true,
                10L,
                SituacaoOferta.RESERVADA,
                dispId,
                new ContatoResponse("Doador", "doador@email.com", "11999990001"),
                new ContatoResponse("ONG", "ong@email.com", "11999990002"),
                dispId != null ? new ContatoResponse("Carlos", "carlos@email.com", "11999990003") : null,
                new EnderecoResponse(null, null, null, null, "Campinas", "SP", null),
                new EnderecoResponse(null, null, null, null, "São Paulo", "SP", null),
                List.of(new ItemDoacaoResponse(1L, "Arroz", TipoItemDoacao.NAO_PERECIVEL, TipoUnidade.KG, 20.0)),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("POST /reservas: deve retornar 201 com Location")
    void reservar_deveRetornar201() throws Exception {
        when(reservaService.reservar(eq(2L), any()))
                .thenReturn(responseExemplo(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE, null));

        mockMvc.perform(post("/reservas")
                        .with(AS_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservaEntregaRequest(10L, true))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/reservas/50"))
                .andExpect(jsonPath("$.situacao").value("AGUARDANDO_TRANSPORTE"));
    }

    @Test
    @DisplayName("GET /reservas/aguardando-transporte: deve retornar 200")
    void listarAguardandoTransporte_deveRetornar200() throws Exception {
        when(reservaService.listarAguardandoTransporte())
                .thenReturn(List.of(responseExemplo(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE, null)));

        mockMvc.perform(get("/reservas/aguardando-transporte").with(AS_TRANSPORTADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50));
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/vincular-transporte: deve retornar 200")
    void vincularTransporte_deveRetornar200() throws Exception {
        when(reservaService.vincularTransporte(eq(3L), eq(50L), any()))
                .thenReturn(responseExemplo(SituacaoReservaEntrega.TRANSPORTE_VINCULADO, 20L));

        mockMvc.perform(patch("/reservas/50/vincular-transporte")
                        .with(AS_TRANSPORTADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VincularTransporteRequest(20L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.disponibilidadeId").value(20))
                .andExpect(jsonPath("$.situacao").value("TRANSPORTE_VINCULADO"));
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/vincular-transporte: conflito de capacidade → 409")
    void vincularTransporte_deveRetornar409() throws Exception {
        when(reservaService.vincularTransporte(eq(3L), eq(50L), any()))
                .thenThrow(new ConflictException("Capacidade insuficiente"));

        mockMvc.perform(patch("/reservas/50/vincular-transporte")
                        .with(AS_TRANSPORTADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VincularTransporteRequest(20L))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/concluir: deve retornar 204")
    void concluir_deveRetornar204() throws Exception {
        doNothing().when(reservaService).concluir(3L, 50L);

        mockMvc.perform(patch("/reservas/50/concluir").with(AS_TRANSPORTADOR))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/cancelar: deve retornar 204")
    void cancelar_deveRetornar204() throws Exception {
        doNothing().when(reservaService).cancelar(2L, 50L);

        mockMvc.perform(patch("/reservas/50/cancelar").with(AS_RECEPTOR))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /reservas/minhas/doador: deve retornar 200")
    void listarMinhasDoador_deveRetornar200() throws Exception {
        when(reservaService.listarMinhasDoador(1L))
                .thenReturn(List.of(responseExemplo(SituacaoReservaEntrega.TRANSPORTE_VINCULADO, 20L)));

        mockMvc.perform(get("/reservas/minhas/doador").with(AS_DOADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(50));
    }
}

