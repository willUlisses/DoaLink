package com.doalink_api.controller;

import com.doalink_api.dtos.request.CampanhaEmergencialRequest;
import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.dtos.request.ItemCampanhaRequest;
import com.doalink_api.dtos.response.CampanhaEmergencialResponse;
import com.doalink_api.dtos.response.ContatoResponse;
import com.doalink_api.dtos.response.EnderecoResponse;
import com.doalink_api.dtos.response.ItemCampanhaResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.entities.enums.SituacaoCampanha;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.exception.ForbiddenException;
import com.doalink_api.exception.GlobalExceptionHandler;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.service.CampanhaEmergencialService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CampanhaEmergencialControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CampanhaEmergencialService campanhaService;

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

    private EnderecoRequest enderecoReq;
    private ItemCampanhaRequest itemReq;
    private CampanhaEmergencialResponse sampleResponse;

    @BeforeEach
    void setUp() {
        CampanhaEmergencialController controller = new CampanhaEmergencialController(campanhaService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver())
                .build();

        enderecoReq = new EnderecoRequest("Rua das Flores", "100", null, "Centro", "São Paulo", "SP", "01001-000");
        itemReq = new ItemCampanhaRequest(TipoItemDoacao.PERECIVEL, TipoUnidade.KG, 50.0, "Alimentos");

        sampleResponse = new CampanhaEmergencialResponse(
                10L,
                "Campanha Chuvas",
                "Arrecadação para famílias atingidas",
                SituacaoCampanha.ATIVA,
                new ContatoResponse("ONG Esperança", "contato@ong.org", "11999998888"),
                new EnderecoResponse("01001-000", "Rua das Flores", "100", null, "Centro", "São Paulo", "SP"),
                List.of(new ItemCampanhaResponse(1L, TipoItemDoacao.PERECIVEL, TipoUnidade.KG, 50.0, "Alimentos")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Deve retornar 201 Created ao criar campanha com sucesso")
    void deveCriarCampanhaComSucesso() throws Exception {
        CampanhaEmergencialRequest req = new CampanhaEmergencialRequest(
                "Campanha Chuvas",
                "Arrecadação para famílias atingidas",
                enderecoReq,
                List.of(itemReq)
        );

        when(campanhaService.criar(eq(1L), any(CampanhaEmergencialRequest.class))).thenReturn(sampleResponse);

        mockMvc.perform(post("/campanhas")
                        .with(AS_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/campanhas/10"))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Campanha Chuvas"))
                .andExpect(jsonPath("$.descricao").value("Arrecadação para famílias atingidas"))
                .andExpect(jsonPath("$.situacao").value("ATIVA"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(50.0));

        verify(campanhaService).criar(eq(1L), any(CampanhaEmergencialRequest.class));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando nome estiver em branco")
    void deveRetornar400QuandoNomeEmBranco() throws Exception {
        CampanhaEmergencialRequest req = new CampanhaEmergencialRequest(
                "",
                "Descrição",
                enderecoReq,
                List.of(itemReq)
        );

        mockMvc.perform(post("/campanhas")
                        .with(AS_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("nome: O nome da campanha é obrigatório")));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando descrição exceder 1000 caracteres")
    void deveRetornar400QuandoDescricaoExcederLimite() throws Exception {
        String descricaoLonga = "A".repeat(1001);
        CampanhaEmergencialRequest req = new CampanhaEmergencialRequest(
                "Campanha Chuvas",
                descricaoLonga,
                enderecoReq,
                List.of(itemReq)
        );

        mockMvc.perform(post("/campanhas")
                        .with(AS_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("descricao: A descrição deve ter no máximo 1000 caracteres")));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request quando endereço for nulo")
    void deveRetornar400QuandoEnderecoNulo() throws Exception {
        CampanhaEmergencialRequest req = new CampanhaEmergencialRequest(
                "Campanha Chuvas",
                "Descrição",
                null,
                List.of(itemReq)
        );

        mockMvc.perform(post("/campanhas")
                        .with(AS_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("enderecoRecebimento: O endereço de recebimento é obrigatório")));
    }

    @Test
    @DisplayName("Deve retornar 403 Forbidden quando service acusar perfil não-receptor")
    void deveRetornar403QuandoNaoForReceptor() throws Exception {
        CampanhaEmergencialRequest req = new CampanhaEmergencialRequest(
                "Campanha Chuvas",
                "Descrição",
                enderecoReq,
                List.of(itemReq)
        );

        when(campanhaService.criar(eq(1L), any(CampanhaEmergencialRequest.class)))
                .thenThrow(new ForbiddenException("Apenas receptores podem criar campanhas"));

        mockMvc.perform(post("/campanhas")
                        .with(AS_RECEPTOR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Apenas receptores podem criar campanhas"));
    }

    @Test
    @DisplayName("Deve retornar 200 OK na listagem de minhas campanhas")
    void deveListarMinhasCampanhas() throws Exception {
        when(campanhaService.listarMinhas(1L)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/campanhas/minhas")
                        .with(AS_RECEPTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].nome").value("Campanha Chuvas"));

        verify(campanhaService).listarMinhas(1L);
    }

    @Test
    @DisplayName("Deve retornar 200 OK na busca por id")
    void deveBuscarPorId() throws Exception {
        when(campanhaService.buscarPorId(10L)).thenReturn(sampleResponse);

        mockMvc.perform(get("/campanhas/10")
                        .with(AS_RECEPTOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.nome").value("Campanha Chuvas"));

        verify(campanhaService).buscarPorId(10L);
    }

    @Test
    @DisplayName("Deve retornar 404 Not Found quando campanha não existir")
    void deveRetornar404QuandoNaoExistir() throws Exception {
        when(campanhaService.buscarPorId(999L))
                .thenThrow(new NotFoundException("Campanha emergencial não encontrada: 999"));

        mockMvc.perform(get("/campanhas/999")
                        .with(AS_RECEPTOR))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Campanha emergencial não encontrada: 999"));
    }
}
