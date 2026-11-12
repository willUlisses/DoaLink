package com.doalink_api.service;

import com.doalink_api.entities.CodigoVerificacaoEmail;
import com.doalink_api.entities.Usuario;
import com.doalink_api.exception.CodigoInvalidoException;
import com.doalink_api.exception.NotFoundException;
import com.doalink_api.repository.CodigoVerificacaoEmailRepository;
import com.doalink_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificacaoEmailService {

    private final UsuarioRepository usuarioRepository;
    private final CodigoVerificacaoEmailRepository codigoRepository;
    private final EmailService emailService;

    @Value("${app.verificacao-email.expiracao-segundos:90}")
    private long expiracaoSegundos;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public void enviarCodigo(Usuario usuario) {
        codigoRepository.revogarPendentesDoUsuario(usuario.getId());

        String codigo = gerarCodigoSeisDigitos();
        String codigoHash = hashSha256(codigo);
        Instant expiracao = Instant.now().plusSeconds(expiracaoSegundos);

        CodigoVerificacaoEmail codigoEntity = new CodigoVerificacaoEmail(usuario, codigoHash, expiracao);
        codigoRepository.save(codigoEntity);

        emailService.enviarCodigoVerificacao(usuario.getEmail(), usuario.getNome(), codigo);
        log.info("Código de verificação enviado para o usuário id={}", usuario.getId());
    }

    @Transactional
    public Usuario verificarCodigo(String email, String codigo) {
        Usuario usuario = usuarioRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        if (usuario.isEmailVerificado()) {
            return usuario;
        }

        CodigoVerificacaoEmail codigoEntity = codigoRepository
                .findFirstByUsuarioIdAndRevogadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(usuario.getId())
                .orElseThrow(() -> new CodigoInvalidoException("Código de verificação inválido ou inexistente"));

        if (codigoEntity.estaExpirado()) {
            codigoEntity.revogar();
            codigoRepository.save(codigoEntity);
            throw new CodigoInvalidoException("Código de verificação expirado. Solicite um novo código.");
        }

        String hashRecebido = hashSha256(codigo);
        if (!codigoEntity.getCodigoHash().equals(hashRecebido)) {
            throw new CodigoInvalidoException("Código de verificação incorreto");
        }

        codigoEntity.marcarUtilizado();
        codigoRepository.save(codigoEntity);

        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        // Limpeza best-effort de expirados
        codigoRepository.deletarExpirados(Instant.now());

        log.info("E-mail verificado com sucesso para usuário id={}", usuario.getId());
        return usuario;
    }

    @Transactional
    public void reenviarCodigo(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.trim().toLowerCase());
        if (usuarioOpt.isEmpty()) {
            log.debug("Solicitação de reenvio para e-mail não cadastrado: {}", email);
            return;
        }

        Usuario usuario = usuarioOpt.get();
        if (usuario.isEmailVerificado()) {
            log.debug("Solicitação de reenvio para e-mail já verificado: {}", email);
            return;
        }

        enviarCodigo(usuario);
    }

    private String gerarCodigoSeisDigitos() {
        int numero = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", numero);
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
