package com.doalink_api.integration;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.request.ReenviarCodigoRequest;
import com.doalink_api.dtos.request.VerificarEmailRequest;
import com.doalink_api.entities.CodigoVerificacaoEmail;
import com.doalink_api.entities.Usuario;
import com.doalink_api.repository.CodigoVerificacaoEmailRepository;
import com.doalink_api.repository.RefreshTokenRepository;
import com.doalink_api.repository.UsuarioRepository;
import com.doalink_api.service.CookieService;
import com.doalink_api.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class VerificacaoEmailIntegrationTest {

    @TestConfiguration
    static class EmailMockConfig {
        @Bean
        @Primary
        public EmailService emailServiceMock(JavaMailSender mailSender) {
            return mock(EmailService.class);
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CodigoVerificacaoEmailRepository codigoRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private MockMvc mockMvc;

    private final String emailTeste = "teste.verificacao@doalink.org";
    private final String senhaTeste = "SenhaForte@123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        reset(emailService);

        usuarioRepository.findByEmail(emailTeste).ifPresent(u -> {
            codigoRepository.deleteAll();
            refreshTokenRepository.revogarTodosDoUsuario(u.getId());
            usuarioRepository.delete(u);
        });
        usuarioRepository.findByDocumento("52998224725").ifPresent(u -> {
            codigoRepository.deleteAll();
            refreshTokenRepository.revogarTodosDoUsuario(u.getId());
            usuarioRepository.delete(u);
        });
        usuarioRepository.findByDocumento("64402685023").ifPresent(u -> {
            codigoRepository.deleteAll();
            refreshTokenRepository.revogarTodosDoUsuario(u.getId());
            usuarioRepository.delete(u);
        });
    }

    @Test
    @DisplayName("CA-010-1 a CA-010-4: Fluxo completo de cadastro, bloqueio de login, confirmação por código e login pós-verificação")
    void deveExecutarFluxoCompletoDeVerificacaoEmail() throws Exception {
        // 1. Cadastro de Doador: deve retornar 201 Created sem cookies e enviar e-mail com código de 6 dígitos
        CadastroComumRequest cadReq = new CadastroComumRequest(
                "52998224725",
                null,
                emailTeste,
                "Maria Verificada",
                senhaTeste
        );

        mockMvc.perform(post("/auth/doadores/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(emailTeste))
                .andExpect(cookie().doesNotExist(CookieService.ACCESS))
                .andExpect(cookie().doesNotExist(CookieService.REFRESH));

        Usuario usuarioSalvo = usuarioRepository.findByEmail(emailTeste).orElseThrow();
        assertThat(usuarioSalvo.isEmailVerificado()).isFalse();

        // Capturar o código enviado por e-mail
        ArgumentCaptor<String> codigoCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).enviarCodigoVerificacao(eq(emailTeste), eq("Maria Verificada"), codigoCaptor.capture());
        String codigoEnviado = codigoCaptor.getValue();
        assertThat(codigoEnviado).matches("^\\d{6}$");

        // 2. CA-010-4: Tentar login antes da verificação deve retornar 403 Forbidden
        LoginRequest loginReq = new LoginRequest(emailTeste, senhaTeste);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("E-mail não verificado. Confirme o código enviado para o seu e-mail antes de acessar a conta."));

        // 3. CA-010-3: Tentar verificar com código incorreto deve retornar 400 Bad Request
        VerificarEmailRequest codIncorretoReq = new VerificarEmailRequest(emailTeste, "999999");
        mockMvc.perform(post("/auth/verificar-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codIncorretoReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Código de verificação incorreto"));

        // 4. CA-010-2: Confirmar com o código correto dentro da validade de 90s deve retornar 200 OK e cookies de sessão
        VerificarEmailRequest codCorretoReq = new VerificarEmailRequest(emailTeste, codigoEnviado);
        mockMvc.perform(post("/auth/verificar-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codCorretoReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(emailTeste))
                .andExpect(cookie().exists(CookieService.ACCESS))
                .andExpect(cookie().exists(CookieService.REFRESH));

        Usuario usuarioPosVerificacao = usuarioRepository.findByEmail(emailTeste).orElseThrow();
        assertThat(usuarioPosVerificacao.isEmailVerificado()).isTrue();

        // 5. CA-010-4: Tentar login agora com a conta verificada deve retornar 200 OK e emitir os cookies
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(emailTeste))
                .andExpect(cookie().exists(CookieService.ACCESS))
                .andExpect(cookie().exists(CookieService.REFRESH));
    }

    @Test
    @DisplayName("CA-010-3: Reenviar código deve revogar o código anterior e gerar um novo código")
    void deveReenviarCodigoRevogandoAnterior() throws Exception {
        CadastroComumRequest cadReq = new CadastroComumRequest(
                "52998224725",
                null,
                emailTeste,
                "Maria Verificada 2",
                senhaTeste
        );

        mockMvc.perform(post("/auth/doadores/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cadReq)))
                .andExpect(status().isCreated());

        Usuario usuario = usuarioRepository.findByEmail(emailTeste).orElseThrow();

        // Solicitar reenvio
        ReenviarCodigoRequest reenviarReq = new ReenviarCodigoRequest(emailTeste);
        mockMvc.perform(post("/auth/reenviar-codigo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reenviarReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Se o e-mail estiver cadastrado e pendente de verificação, um novo código foi enviado."));

        // Verificar que emailService foi chamado 2 vezes (cadastro + reenvio)
        verify(emailService, times(2)).enviarCodigoVerificacao(eq(emailTeste), eq("Maria Verificada 2"), anyString());

        // Verificar que o primeiro código foi revogado
        entityManager.clear();
        List<CodigoVerificacaoEmail> codigosDoUsuario = codigoRepository.findAll().stream()
                .filter(c -> c.getUsuario().getId().equals(usuario.getId()))
                .toList();
        long revogados = codigosDoUsuario.stream().filter(CodigoVerificacaoEmail::isRevogado).count();
        long ativos = codigosDoUsuario.stream().filter(c -> !c.isRevogado() && !c.isUtilizado()).count();

        assertThat(revogados).isEqualTo(1);
        assertThat(ativos).isEqualTo(1);
    }
}
