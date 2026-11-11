package com.doalink_api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "token_recuperacao_senha")
@Getter
@Setter
@NoArgsConstructor
public class TokenRecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiracao;

    @Column(nullable = false)
    private boolean utilizado = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    public TokenRecuperacaoSenha(Usuario usuario, String tokenHash, Instant expiracao) {
        this.usuario = usuario;
        this.tokenHash = tokenHash;
        this.expiracao = expiracao;
    }

    public boolean estaExpirado() {
        return Instant.now().isAfter(expiracao);
    }

    public boolean estaValido() {
        return !utilizado && !estaExpirado();
    }
}
