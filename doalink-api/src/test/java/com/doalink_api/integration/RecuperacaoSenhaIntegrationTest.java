package com.doalink_api.integration;

import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.request.RedefinirSenhaRequest;
import com.doalink_api.dtos.request.SolicitarRecuperacaoSenhaRequest;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.TokenRecuperacaoSenha;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.repository.DoadorRepository;
import com.doalink_api.repository.RefreshTokenRepository;
import com.doalink_api.repository.TokenRecuperacaoSenhaRepository;
import com.doalink_api.repository.UsuarioRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RecuperacaoSenhaIntegrationTest {

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
    private DoadorRepository doadorRepository;

    @Autowired
    private TokenRecuperacaoSenhaRepository tokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private MockMvc mockMvc;

    private Doador doador;
    private final String emailTeste = "doador.recuperacao@doalink.org";
    private final String senhaInicial = "SenhaOriginal@123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        reset(emailService);

        // Criar usuário para os testes se não existir
        usuarioRepository.findByEmail(emailTeste).ifPresent(u -> {
            tokenRepository.deleteAll();
            refreshTokenRepository.revogarTodosDoUsuario(u.getId());
            usuarioRepository.delete(u);
        });

        doador = new Doador(
                "98765432100",
                "Carlos do Doador",
                emailTeste,
                passwordEncoder.encode(senhaInicial),
                "11987654321",
                TipoPessoa.FISICA
        );
        doador = doadorRepository.save(doador);
    }

    private String calcularHashSha256(String token) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }

    @Test
    @DisplayName("CA-003-2: Solicitar recuperação para e-mail não cadastrado retorna 200 OK sem enviar e-mail")
    void deveResponderOkUniformeQuandoEmailNaoCadastrado() throws Exception {
        SolicitarRecuperacaoSenhaRequest req = new SolicitarRecuperacaoSenhaRequest("desconhecido@doalink.org");

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Se o e-mail informado estiver cadastrado, você receberá um link para redefinir sua senha em breve."));

        verify(emailService, never()).enviarRecuperacaoSenha(any(), any(), any());
    }

    @Test
    @DisplayName("CA-003-1, CA-003-3, CA-003-5, CA-003-6: Fluxo completo de recuperação de senha com sucesso")
    void deveExecutarFluxoCompletoDeRecuperacaoComSucesso() throws Exception {
        // 1. CA-003-1: Solicitar recuperação para e-mail cadastrado
        SolicitarRecuperacaoSenhaRequest solReq = new SolicitarRecuperacaoSenhaRequest(emailTeste);

        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Se o e-mail informado estiver cadastrado, você receberá um link para redefinir sua senha em breve."));

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).enviarRecuperacaoSenha(
                eq(emailTeste),
                eq("Carlos do Doador"),
                tokenCaptor.capture()
        );

        String tokenOpaco = tokenCaptor.getValue();
        assertThat(tokenOpaco).isNotBlank();

        // 2. Validar token
        mockMvc.perform(get("/auth/recuperar-senha/validar")
                        .param("token", tokenOpaco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(true));

        // 3. CA-003-3: Redefinir senha com nova senha válida
        String novaSenha = "NovaSenhaSegura@2026";
        RedefinirSenhaRequest redefReq = new RedefinirSenhaRequest(tokenOpaco, novaSenha);

        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(redefReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Senha redefinida com sucesso."));

        // 4. CA-003-5: Login com nova senha deve suceder
        LoginRequest loginComNovaSenha = new LoginRequest(emailTeste, novaSenha);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginComNovaSenha)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(emailTeste))
                .andExpect(jsonPath("$.nome").value("Carlos do Doador"));

        // 5. CA-003-6: Login com a senha antiga deve falhar
        LoginRequest loginComSenhaAntiga = new LoginRequest(emailTeste, senhaInicial);
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginComSenhaAntiga)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("CA-003-4: Reutilizar o mesmo token já utilizado deve ser rejeitado com 404")
    void deveRejeitarReutilizacaoDeToken() throws Exception {
        // Solicita recuperação
        mockMvc.perform(post("/auth/recuperar-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SolicitarRecuperacaoSenhaRequest(emailTeste))))
                .andExpect(status().isOk());

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).enviarRecuperacaoSenha(any(), any(), tokenCaptor.capture());
        String tokenOpaco = tokenCaptor.getValue();

        // Primeira redefinição: sucesso
        RedefinirSenhaRequest redef1 = new RedefinirSenhaRequest(tokenOpaco, "PrimeiraTroca@123");
        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(redef1)))
                .andExpect(status().isOk());

        // Segunda redefinição com mesmo token: falha
        RedefinirSenhaRequest redef2 = new RedefinirSenhaRequest(tokenOpaco, "SegundaTroca@123");
        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(redef2)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("CA-003-4: Token expirado deve ser rejeitado na validação e na redefinição")
    void deveRejeitarTokenExpirado() throws Exception {
        String tokenOpaco = "token-expirado-1234567890";
        String tokenHash = calcularHashSha256(tokenOpaco);

        TokenRecuperacaoSenha tokenExpirado = new TokenRecuperacaoSenha(
                doador,
                tokenHash,
                Instant.now().minus(2, ChronoUnit.MINUTES)
        );
        tokenRepository.save(tokenExpirado);

        // Validação retorna valido=false
        mockMvc.perform(get("/auth/recuperar-senha/validar")
                        .param("token", tokenOpaco))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false));

        // Redefinição falha com 404
        RedefinirSenhaRequest redefReq = new RedefinirSenhaRequest(tokenOpaco, "TentativaSenha@123");
        mockMvc.perform(post("/auth/redefinir-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(redefReq)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
