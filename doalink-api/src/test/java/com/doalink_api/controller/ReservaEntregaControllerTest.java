package com.doalink_api.controller;

import com.doalink_api.dtos.request.ConfirmarCodigoTransporteRequest;
import com.doalink_api.dtos.request.ReservaEntregaRequest;
import com.doalink_api.dtos.response.*;
import com.doalink_api.entities.enums.SituacaoOferta;
import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.exception.CodigoInvalidoException;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.ForbiddenException;
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

    private ReservaEntregaResponse responseExemplo(SituacaoReservaEntrega situacao, Long transportadorId) {
        return new ReservaEntregaResponse(
                50L,
                situacao,
                true,
                10L,
                SituacaoOferta.RESERVADA,
                transportadorId,
                new ContatoResponse("Doador", "doador@email.com", "11999990001"),
                new ContatoResponse("ONG", "ong@email.com", "11999990002"),
                transportadorId != null ? new ContatoResponse("Carlos", "carlos@email.com", "11999990003") : null,
                new EnderecoResponse(null, null, null, null, "Campinas", "SP", null),
                new EnderecoResponse(null, null, null, null, "São Paulo", "SP", null),
                List.of(new ItemDoacaoResponse(1L, "Arroz", TipoItemDoacao.NAO_PERECIVEL, TipoUnidade.KG, 20.0)),
                LocalDateTime.now(),
                null,
                null,
                null,
                situacao == SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA ? "123456" : null,
                situacao == SituacaoReservaEntrega.EM_TRANSPORTE ? "654321" : null,
                situacao == SituacaoReservaEntrega.EM_TRANSPORTE ? LocalDateTime.now().plusHours(24) : null,
                List.of(),
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
    @DisplayName("PATCH /reservas/{id}/aceitar-transporte: deve retornar 200")
    void aceitarTransporte_deveRetornar200() throws Exception {
        when(reservaService.aceitarTransporte(3L, 50L))
                .thenReturn(responseExemplo(SituacaoReservaEntrega.AGUARDANDO_AUTORIZACAO_DOADOR, 3L));

        mockMvc.perform(patch("/reservas/50/aceitar-transporte").with(AS_TRANSPORTADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transportadorId").value(3))
                .andExpect(jsonPath("$.situacao").value("AGUARDANDO_AUTORIZACAO_DOADOR"));
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/aceitar-transporte: conflito simultâneo → 409")
    void aceitarTransporte_deveRetornar409() throws Exception {
        when(reservaService.aceitarTransporte(3L, 50L))
                .thenThrow(new ConflictException("Reserva já assumida"));

        mockMvc.perform(patch("/reservas/50/aceitar-transporte").with(AS_TRANSPORTADOR))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/autorizar-transporte: doador autoriza e recebe código → 200")
    void autorizarInicio_deveRetornar200() throws Exception {
        when(reservaService.autorizarInicioTransporte(1L, 50L))
                .thenReturn(responseExemplo(SituacaoReservaEntrega.AGUARDANDO_CONFIRMACAO_RETIRADA, 3L));

        mockMvc.perform(patch("/reservas/50/autorizar-transporte").with(AS_DOADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("AGUARDANDO_CONFIRMACAO_RETIRADA"))
                .andExpect(jsonPath("$.codigoRetirada").value("123456"));
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/confirmar-retirada: código válido → 200; código inválido → 400")
    void confirmarRetirada_deveValidarCodigo() throws Exception {
        when(reservaService.confirmarRetirada(eq(3L), eq(50L), any()))
                .thenReturn(responseExemplo(SituacaoReservaEntrega.EM_TRANSPORTE, 3L))
                .thenThrow(new CodigoInvalidoException("Código de retirada inválido"));

        mockMvc.perform(patch("/reservas/50/confirmar-retirada")
                        .with(AS_TRANSPORTADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarCodigoTransporteRequest("123456"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_TRANSPORTE"));

        mockMvc.perform(patch("/reservas/50/confirmar-retirada")
                        .with(AS_TRANSPORTADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarCodigoTransporteRequest("000000"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/confirmar-entrega: código expirado/suspenso → 403")
    void confirmarEntrega_expirado_deveRetornar403() throws Exception {
        when(reservaService.confirmarEntrega(eq(3L), eq(50L), any()))
                .thenThrow(new ForbiddenException("Operação bloqueada e conta sob investigação"));

        mockMvc.perform(patch("/reservas/50/confirmar-entrega")
                        .with(AS_TRANSPORTADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarCodigoTransporteRequest("654321"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /reservas/minhas/transportador/historico: deve retornar 200")
    void listarHistoricoTransportador_deveRetornar200() throws Exception {
        when(reservaService.listarHistoricoTransportador(3L))
                .thenReturn(List.of(responseExemplo(SituacaoReservaEntrega.CONCLUIDA, 3L)));

        mockMvc.perform(get("/reservas/minhas/transportador/historico").with(AS_TRANSPORTADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].situacao").value("CONCLUIDA"));
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/concluir: deve retornar 204")
    void concluir_deveRetornar204() throws Exception {
        doNothing().when(reservaService).concluir(2L, 50L);

        mockMvc.perform(patch("/reservas/50/concluir").with(AS_RECEPTOR))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /reservas/{id}/cancelar: deve retornar 204")
    void cancelar_deveRetornar204() throws Exception {
        doNothing().when(reservaService).cancelar(2L, 50L);

        mockMvc.perform(patch("/reservas/50/cancelar").with(AS_RECEPTOR))
                .andExpect(status().isNoContent());
    }
}
