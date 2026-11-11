package com.doalink_api.controller;

import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.request.ItemNecessidadeRequest;
import com.doalink_api.dtos.request.NecessidadeDoacaoRequest;
import com.doalink_api.dtos.response.*;
import com.doalink_api.entities.enums.SituacaoNecessidade;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.GlobalExceptionHandler;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.service.NecessidadeDoacaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class   NecessidadeDoacaoControllerTest {

        private MockMvc mockMvc;

        @Mock
        private NecessidadeDoacaoService necessidadeService;

        private final ObjectMapper objectMapper = new ObjectMapper();

        private static final UsuarioAutenticado RECEPTOR_PRINCIPAL = new UsuarioAutenticado(1L, "receptor@email.com");

        private static final RequestPostProcessor AS_RECEPTOR = request -> {
                var auth = new UsernamePasswordAuthenticationToken(
                                RECEPTOR_PRINCIPAL,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_RECEPTOR")));
                SecurityContextHolder.setContext(new SecurityContextImpl(auth));
                return request;
        };

        @BeforeEach
        void setUp() {
                NecessidadeDoacaoController controller = new NecessidadeDoacaoController(necessidadeService);
                mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                .setControllerAdvice(new GlobalExceptionHandler())
                                .setCustomArgumentResolvers(
                                                new AuthenticationPrincipalArgumentResolver(),
                                                new PageableHandlerMethodArgumentResolver())
                                .build();
        }

        private NecessidadeDoacaoResponse responseExemplo() {
                ItemNecessidadeResponse item = new ItemNecessidadeResponse(
                                1L, "Arroz", TipoItemDoacao.NAO_PERECIVEL, TipoUnidade.KG, 50.0);
                EnderecoResponse endereco = new EnderecoResponse(
                                "Rua das Flores", "10", null, "Centro", "São Paulo", "SP", "01310100");
                ContatoResponse contato = new ContatoResponse("ONG Esperança", "ong@email.com", "11988887777");
                return new NecessidadeDoacaoResponse(
                                1L, SituacaoNecessidade.ABERTA, contato, endereco, List.of(item),
                                LocalDateTime.now(), LocalDateTime.now());
        }

        private NecessidadeDoacaoRequest reqValida() {
                ItemNecessidadeRequest item = new ItemNecessidadeRequest(
                                "Arroz", TipoItemDoacao.NAO_PERECIVEL, TipoUnidade.KG, 50.0);
                EnderecoRequest endereco = new EnderecoRequest(
                                "Rua das Flores", "10", null, "Centro", "São Paulo", "SP", "01310-100");
                return new NecessidadeDoacaoRequest(List.of(item), endereco);
        }

        @Test
        @DisplayName("POST /necessidades: dados válidos e perfil receptor → 201 Created com Location")
        void criar_dadosValidos_deveRetornar201() throws Exception {
                when(necessidadeService.criar(eq(1L), any(NecessidadeDoacaoRequest.class)))
                                .thenReturn(responseExemplo());

                mockMvc.perform(post("/necessidades")
                                .with(AS_RECEPTOR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqValida())))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.situacao").value("ABERTA"))
                                .andExpect(jsonPath("$.itens[0].tipoUnidade").value("KG"));

                verify(necessidadeService).criar(eq(1L), any(NecessidadeDoacaoRequest.class));
        }

        @Test
        @DisplayName("POST /necessidades: itens vazios → 400 Bad Request")
        void criar_itensVazios_deveRetornar400() throws Exception {
                EnderecoRequest endereco = new EnderecoRequest(
                                "Rua das Flores", "10", null, "Centro", "São Paulo", "SP", "01310-100");
                NecessidadeDoacaoRequest reqInvalida = new NecessidadeDoacaoRequest(List.of(), endereco);

                mockMvc.perform(post("/necessidades")
                                .with(AS_RECEPTOR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqInvalida)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("POST /necessidades: quantidade <= 0 → 400 Bad Request")
        void criar_quantidadeZero_deveRetornar400() throws Exception {
                ItemNecessidadeRequest itemInvalido = new ItemNecessidadeRequest(
                                "Arroz", TipoItemDoacao.NAO_PERECIVEL, TipoUnidade.KG, 0.0);
                EnderecoRequest endereco = new EnderecoRequest(
                                "Rua das Flores", "10", null, "Centro", "São Paulo", "SP", "01310-100");
                NecessidadeDoacaoRequest req = new NecessidadeDoacaoRequest(List.of(itemInvalido), endereco);

                mockMvc.perform(post("/necessidades")
                                .with(AS_RECEPTOR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("POST /necessidades: não receptor → 403 Forbidden")
        void criar_naoReceptor_deveRetornar403() throws Exception {
                when(necessidadeService.criar(eq(1L), any(NecessidadeDoacaoRequest.class)))
                                .thenThrow(new ForbiddenException("Apenas receptores podem criar necessidades"));

                mockMvc.perform(post("/necessidades")
                                .with(AS_RECEPTOR)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(reqValida())))
                                .andExpect(status().isForbidden())
                                .andExpect(jsonPath("$.message").value("Apenas receptores podem criar necessidades"));
        }

        @Test
        @DisplayName("GET /necessidades/minhas: retorna lista 200 OK")
        void listarMinhas_deveRetornar200() throws Exception {
                when(necessidadeService.listarMinhas(1L)).thenReturn(List.of(responseExemplo()));

                mockMvc.perform(get("/necessidades/minhas")
                                .with(AS_RECEPTOR))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1L));

                verify(necessidadeService).listarMinhas(1L);
        }

        @Test
        @DisplayName("GET /necessidades: consulta paginada → 200 OK")
        void listar_deveRetornar200() throws Exception {
                org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0,
                                20);
                when(necessidadeService.listarPorSituacao(eq(SituacaoNecessidade.ABERTA), any()))
                                .thenReturn(new PageImpl<>(List.of(responseExemplo()), pageable, 1));

                mockMvc.perform(get("/necessidades"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].id").value(1L));
        }

        @Test
        @DisplayName("GET /necessidades?situacao=INVALIDA: parâmetro inválido → 400 Bad Request")
        void listar_situacaoInvalida_deveRetornar400() throws Exception {
                mockMvc.perform(get("/necessidades").param("situacao", "INVALIDA"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message")
                                                .value("Parâmetro 'situacao' recebeu um valor inválido: 'INVALIDA'"));
        }

        @Test
        @DisplayName("GET /necessidades/{id}: existente → 200 OK")
        void buscarPorId_existente_deveRetornar200() throws Exception {
                when(necessidadeService.buscarPorId(1L)).thenReturn(responseExemplo());

                mockMvc.perform(get("/necessidades/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("GET /necessidades/{id}: inexistente → 404 Not Found")
        void buscarPorId_inexistente_deveRetornar404() throws Exception {
                when(necessidadeService.buscarPorId(99L))
                                .thenThrow(new NotFoundException("Necessidade não encontrada: 99"));

                mockMvc.perform(get("/necessidades/99"))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.message").value("Necessidade não encontrada: 99"));
        }
}
