package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoCodigoTransporte;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "codigos_transporte")
public class CodigoTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private ReservaEntrega reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transportador_id", nullable = false)
    private Transportador transportador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCodigoTransporte tipo;

    @Column(name = "codigo_hash", nullable = false, length = 64)
    private String codigoHash;

    @Column(name = "codigo_salt", nullable = false, length = 64)
    private String codigoSalt;

    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    @Column(nullable = false)
    private boolean utilizado = false;

    @Column(nullable = false)
    private boolean invalidado = false;

    @Column(nullable = false)
    private boolean bloqueado = false;

    @Column(nullable = false)
    private int tentativas = 0;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "utilizado_em")
    private LocalDateTime utilizadoEm;

    public CodigoTransporte(
            ReservaEntrega reserva,
            Transportador transportador,
            TipoCodigoTransporte tipo,
            String codigoHash,
            String codigoSalt,
            LocalDateTime expiraEm) {
        this.reserva = reserva;
        this.transportador = transportador;
        this.tipo = tipo;
        this.codigoHash = codigoHash;
        this.codigoSalt = codigoSalt;
        this.expiraEm = expiraEm;
    }

    @PrePersist
    private void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }

    public boolean estaExpirado(LocalDateTime agora) {
        return expiraEm != null && agora.isAfter(expiraEm);
    }

    public boolean estaValido(LocalDateTime agora) {
        return !utilizado && !invalidado && !bloqueado && !estaExpirado(agora);
    }

    public void marcarUtilizado(LocalDateTime agora) {
        this.utilizado = true;
        this.invalidado = true;
        this.utilizadoEm = agora;
    }

    public void invalidar() {
        this.invalidado = true;
    }

    public void bloquear() {
        this.bloqueado = true;
        this.invalidado = true;
    }
}
