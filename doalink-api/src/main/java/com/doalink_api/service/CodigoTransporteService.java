package com.doalink_api.service;

import com.doalink_api.entities.CodigoTransporte;
import com.doalink_api.entities.ReservaEntrega;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.enums.TipoCodigoTransporte;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
public class CodigoTransporteService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DEFAULT_DEV_SECRET = "doalink-secret-jwt-key-for-development-32charsmin";

    private final String segredoHmac;
    private final int maxTentativas;
    private final long validadeEntregaHoras;

    public CodigoTransporteService(
            @Value("${app.jwt.secret:" + DEFAULT_DEV_SECRET + "}") String segredoHmac,
            @Value("${app.transporte.codigo.max-tentativas:5}") int maxTentativas,
            @Value("${app.transporte.codigo-entrega.validade-horas:24}") long validadeEntregaHoras) {
        this.segredoHmac = (segredoHmac == null || segredoHmac.isBlank()) ? DEFAULT_DEV_SECRET : segredoHmac;
        this.maxTentativas = maxTentativas > 0 ? maxTentativas : 5;
        this.validadeEntregaHoras = validadeEntregaHoras > 0 ? validadeEntregaHoras : 24;
    }

    public int getMaxTentativas() {
        return maxTentativas;
    }

    public long getValidadeEntregaHoras() {
        return validadeEntregaHoras;
    }

    public CodigoTransporte criarCodigo(
            ReservaEntrega reserva,
            Transportador transportador,
            TipoCodigoTransporte tipo,
            LocalDateTime expiraEm) {
        String salt = gerarSaltHex();
        String codigoPlano = derivarCodigoSeisDigitos(salt, reserva.getId(), tipo);
        String codigoHash = hashSha256(codigoPlano);
        return new CodigoTransporte(reserva, transportador, tipo, codigoHash, salt, expiraEm);
    }

    public String reconstruirCodigoAtivo(CodigoTransporte codigoEntity, LocalDateTime agora) {
        if (codigoEntity == null || !codigoEntity.estaValido(agora)) {
            return null;
        }
        return derivarCodigoSeisDigitos(
                codigoEntity.getCodigoSalt(),
                codigoEntity.getReserva().getId(),
                codigoEntity.getTipo()
        );
    }

    public boolean validarCodigo(CodigoTransporte codigoEntity, String codigoInformado) {
        if (codigoEntity == null || codigoInformado == null) {
            return false;
        }
        String hashInformado = hashSha256(codigoInformado.trim());
        return MessageDigest.isEqual(
                hashInformado.getBytes(StandardCharsets.UTF_8),
                codigoEntity.getCodigoHash().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String gerarSaltHex() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    private String derivarCodigoSeisDigitos(String salt, Long reservaId, TipoCodigoTransporte tipo) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(segredoHmac.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            String payload = salt + ":" + reservaId + ":" + tipo.name();
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            int valor = ((hmac[0] & 0x7F) << 24)
                    | ((hmac[1] & 0xFF) << 16)
                    | ((hmac[2] & 0xFF) << 8)
                    | (hmac[3] & 0xFF);
            return String.format("%06d", valor % 1_000_000);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Falha ao derivar código de transporte", e);
        }
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
