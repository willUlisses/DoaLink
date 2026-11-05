package com.doalink_api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.doalink_api.entities.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Service
public class JwtService {

    private static final String ISSUER = "doalink-api";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long accessMinutes;


    public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-minutes}") long accessMinutes) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
        this.accessMinutes = accessMinutes;
    }

    public String gerarAccessToken(Usuario u) {
        Instant agora = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(u.getId().toString())
                .withClaim("email", u.getEmail())
                .withClaim("role", u.getTipoUsuario().name())
                .withIssuedAt(agora)
                .withExpiresAt(agora.plus(Duration.ofMinutes(accessMinutes)))
                .sign(algorithm);
    }

    public Optional<DecodedJWT> validar(String token) {
        try {
            return Optional.of(verifier.verify(token)); // esse verifier aqui já ta buildado do construtor com o algoritmo e issuer
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

}
