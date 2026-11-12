package com.doalink_api.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "codigos_verificacao_email")
@Getter
@Setter
@NoArgsConstructor
public class CodigoVerificacaoEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "codigo_hash", nullable = false, length = 64)
    private String codigoHash;

    @Column(nullable = false)
    private Instant expiracao;

    @Column(nullable = false)
    private boolean utilizado = false;

    @Column(nullable = false)
    private boolean revogado = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm = Instant.now();

    public CodigoVerificacaoEmail(Usuario usuario, String codigoHash, Instant expiracao) {
        this.usuario = usuario;
        this.codigoHash = codigoHash;
        this.expiracao = expiracao;
    }

    public boolean estaExpirado() {
        return Instant.now().isAfter(expiracao);
    }

    public boolean estaValido() {
        return !utilizado && !revogado && !estaExpirado();
    }

    public void marcarUtilizado() {
        this.utilizado = true;
    }

    public void revogar() {
        this.revogado = true;
    }
}
