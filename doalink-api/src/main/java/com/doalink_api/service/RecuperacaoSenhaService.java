package com.doalink_api.service;

import com.doalink_api.entities.TokenRecuperacaoSenha;
import com.doalink_api.entities.Usuario;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.RefreshTokenRepository;
import com.doalink_api.repository.TokenRecuperacaoSenhaRepository;
import com.doalink_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecuperacaoSenhaService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacaoSenhaRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.recuperacao-senha.expiracao-minutos:10}")
    private long expiracaoMinutos;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public void solicitarRecuperacao(String email) {
        // Resposta uniforme: não revelamos se o e-mail existe (proteção contra enumeração)
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim().toLowerCase());
        if (usuarioOpt.isEmpty()) {
            log.debug("Solicitação de recuperação para e-mail inexistente: {}", email);
            return;
        }

        Usuario usuario = usuarioOpt.get();
        String tokenOpaco = gerarTokenOpaco();
        String tokenHash = hashSha256(tokenOpaco);
        Instant expiracao = Instant.now().plus(expiracaoMinutos, ChronoUnit.MINUTES);

        tokenRepository.save(new TokenRecuperacaoSenha(usuario, tokenHash, expiracao));
        emailService.enviarRecuperacaoSenha(usuario.getEmail(), usuario.getNome(), tokenOpaco);
    }

    @Transactional(readOnly = true)
    public boolean validarToken(String token) {
        String tokenHash = hashSha256(token);
        return tokenRepository.findByTokenHash(tokenHash)
                .map(TokenRecuperacaoSenha::estaValido)
                .orElse(false);
    }

    @Transactional
    public void redefinirSenha(String token, String novaSenha) {
        String tokenHash = hashSha256(token);
        TokenRecuperacaoSenha tokenEntity = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new NotFoundException("Token de recuperação inválido ou expirado"));

        if (!tokenEntity.estaValido()) {
            throw new NotFoundException("Token de recuperação inválido ou expirado");
        }

        Usuario usuario = tokenEntity.getUsuario();
        usuario.setSenha(passwordEncoder.encode(novaSenha));

        // Revogar todos os refresh tokens do usuário
        refreshTokenRepository.revogarTodosDoUsuario(usuario.getId());

        // Marcar token como utilizado
        tokenEntity.setUtilizado(true);

        // Limpar tokens expirados (best-effort)
        tokenRepository.deletarExpirados(Instant.now());

        log.info("Senha redefinida com sucesso para usuário id={}", usuario.getId());
    }

    private String gerarTokenOpaco() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 não disponível", e);
        }
    }
}
