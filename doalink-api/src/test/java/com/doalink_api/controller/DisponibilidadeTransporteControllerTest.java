package com.doalink_api.controller;

import com.doalink_api.dtos.request.DisponibilidadeTransporteRequest;
import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.response.ContatoResponse;
import com.doalink_api.dtos.response.DisponibilidadeTransporteResponse;
import com.doalink_api.dtos.response.EnderecoResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.entities.enums.SituacaoDisponibilidade;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.entities.enums.TipoVeiculo;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.GlobalExceptionHandler;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.service.DisponibilidadeTransporteService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DisponibilidadeTransporteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DisponibilidadeTransporteService disponibilidadeService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final UsuarioAutenticado TRANSPORTADOR_PRINCIPAL =
            new UsuarioAutenticado(1L, "transportador@email.com");

    private static final RequestPostProcessor AS_TRANSPORTADOR = request -> {
        var auth = new UsernamePasswordAuthenticationToken(
                TRANSPORTADOR_PRINCIPAL, null,
                List.of(new SimpleGrantedAuthority("ROLE_TRANSPORTADOR")));
        var ctx = new SecurityContextImpl(auth);
        SecurityContextHolder.setContext(ctx);
        request.setAttribute("SPRING_SECURITY_CONTEXT", ctx);
        return request;
    };

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new DisponibilidadeTransporteController(disponibilidadeService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    private DisponibilidadeTransporteResponse responseExemplo() {
        EnderecoResponse origem = new EnderecoResponse(
                "Rua das Flores", "100", null, "Centro", "São Paulo", "SP", "01310100");
        ContatoResponse contato = new ContatoResponse("Carlos", "carlos@email.com", "11999999999");
        return new DisponibilidadeTransporteResponse(
                10L,
                SituacaoDisponibilidade.DISPONIVEL,
                contato,
                TipoVeiculo.UTILITARIO_LEVE,
                500.0,
                500.0,
                TipoUnidade.KG,
                origem,
                LocalDate.now().plusDays(2),
                LocalTime.of(9, 0),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private DisponibilidadeTransporteRequest reqValida() {
        EnderecoRequest origem = new EnderecoRequest(
                "Rua das Flores", "100", null, "Centro", "São Paulo", "SP", "01310-100");
        return new DisponibilidadeTransporteRequest(
                TipoVeiculo.UTILITARIO_LEVE,
                500.0,
                TipoUnidade.KG,
                origem,
                LocalDate.now().plusDays(2),
                LocalTime.of(9, 0)
        );
    }

    @Test
    @DisplayName("POST /disponibilidades: deve retornar 201 com Location e body")
    void criar_deveRetornar201() throws Exception {
        when(disponibilidadeService.criar(eq(1L), any())).thenReturn(responseExemplo());

        mockMvc.perform(post("/disponibilidades")
                        .with(AS_TRANSPORTADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqValida())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/disponibilidades/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.situacao").value("DISPONIVEL"))
                .andExpect(jsonPath("$.tipoVeiculo").value("UTILITARIO_LEVE"))
                .andExpect(jsonPath("$.capacidadeRestante").value(500.0));
    }

    @Test
    @DisplayName("POST /disponibilidades: deve retornar 400 para capacidade <= 0 ou data passada")
    void criar_deveRetornar400QuandoDadosInvalidos() throws Exception {
        EnderecoRequest origem = new EnderecoRequest(
                "Rua das Flores", "100", null, "Centro", "São Paulo", "SP", "01310-100");
        var reqInvalida = new DisponibilidadeTransporteRequest(
                TipoVeiculo.MOTO,
                0.0,
                TipoUnidade.KG,
                origem,
                LocalDate.now().minusDays(1),
                null
        );

        mockMvc.perform(post("/disponibilidades")
                        .with(AS_TRANSPORTADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqInvalida)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(disponibilidadeService);
    }

    @Test
    @DisplayName("POST /disponibilidades: deve retornar 409 quando data já ocupada")
    void criar_deveRetornar409QuandoConflito() throws Exception {
        when(disponibilidadeService.criar(eq(1L), any()))
                .thenThrow(new ConflictException("Já existe uma disponibilidade ativa"));

        mockMvc.perform(post("/disponibilidades")
                        .with(AS_TRANSPORTADOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqValida())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /disponibilidades/minhas: deve retornar 200 com lista do transportador")
    void listarMinhas_deveRetornar200() throws Exception {
        when(disponibilidadeService.listarMinhas(1L)).thenReturn(List.of(responseExemplo()));

        mockMvc.perform(get("/disponibilidades/minhas").with(AS_TRANSPORTADOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    @DisplayName("PATCH /disponibilidades/{id}/cancelar: deve retornar 204")
    void cancelar_deveRetornar204() throws Exception {
        doNothing().when(disponibilidadeService).cancelar(1L, 10L);

        mockMvc.perform(patch("/disponibilidades/10/cancelar").with(AS_TRANSPORTADOR))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /disponibilidades/{id}/cancelar: deve retornar 403 quando ForbiddenException")
    void cancelar_deveRetornar403() throws Exception {
        doThrow(new ForbiddenException("Sem permissão")).when(disponibilidadeService).cancelar(1L, 10L);

        mockMvc.perform(patch("/disponibilidades/10/cancelar").with(AS_TRANSPORTADOR))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /disponibilidades/{id}/cancelar: deve retornar 404 quando NotFoundException")
    void cancelar_deveRetornar404() throws Exception {
        doThrow(new NotFoundException("Não encontrada")).when(disponibilidadeService).cancelar(1L, 999L);

        mockMvc.perform(patch("/disponibilidades/999/cancelar").with(AS_TRANSPORTADOR))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /disponibilidades/{id}/encerrar: deve retornar 204")
    void encerrar_deveRetornar204() throws Exception {
        doNothing().when(disponibilidadeService).encerrar(1L, 10L);

        mockMvc.perform(patch("/disponibilidades/10/encerrar").with(AS_TRANSPORTADOR))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /disponibilidades/{id}/encerrar: deve retornar 409 quando ConflictException")
    void encerrar_deveRetornar409() throws Exception {
        doThrow(new ConflictException("Pendências")).when(disponibilidadeService).encerrar(1L, 10L);

        mockMvc.perform(patch("/disponibilidades/10/encerrar").with(AS_TRANSPORTADOR))
                .andExpect(status().isConflict());
    }
}

