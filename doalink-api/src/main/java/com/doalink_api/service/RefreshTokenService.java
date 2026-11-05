package com.doalink_api.service;

import com.doalink_api.entities.RefreshToken;
import com.doalink_api.entities.Usuario;
import com.doalink_api.exception.TokenInvalidoException;
import com.doalink_api.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public record Rotacao(Usuario usuario, String novoRefreshToken) { }

    private static final SecureRandom RANDOM = new SecureRandom();

    @Value("${app.refresh.days}")
    private long dias;

    @Transactional
    public String criar(Usuario usuario) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes); // to puxando uma sequencia randomica de bytes e construo o token aq abaixo com Base64 e encodando usando essa sequencia dos bytes
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken rt = new RefreshToken();
        rt.setTokenHash(hash(token));
        rt.setUsuario(usuario);
        rt.setExpiraEm(Instant.now().plus(Duration.ofDays(dias)));
        repository.save(rt);
        return token;
    }

    @Transactional(noRollbackFor = TokenInvalidoException.class)
    public Rotacao rotacionar(String token) {
        RefreshToken rt = repository.findByTokenHash(hash(token))
                .orElseThrow(() -> new TokenInvalidoException("Token invalido"));

        if (rt.isRevogado()) {
            repository.revogarTodosDoUsuario(rt.getUsuario().getId());
            throw new TokenInvalidoException("Token já usado");
        }
        if (rt.getExpiraEm().isBefore(Instant.now())) {
            throw new TokenInvalidoException("Token expirado");
        }

        rt.setRevogado(true);
        Usuario usuario = rt.getUsuario();
        return new Rotacao(usuario, criar(usuario)); // aqui o objetivo é revogar um refresh token antigo e gerar um novo pro usuário (rotação)
    }

    @Transactional
    public void revogar(String token) {
        repository.findByTokenHash(hash(token)).ifPresent(rt -> rt.setRevogado(true));
    }

    @Transactional
    public void revogarTodos(Long usuarioId) {
        repository.revogarTodosDoUsuario(usuarioId);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void limparExpirados() {
        repository.deletarExpirados(Instant.now());
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
