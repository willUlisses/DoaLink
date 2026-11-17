package com.doalink_api.entities;

import com.doalink_api.entities.enums.SituacaoReservaEntrega;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reservas_entrega")
public class ReservaEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaDoacao oferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptor_id", nullable = false)
    private Receptor receptor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "disponibilidade_id")
    private DisponibilidadeTransporte disponibilidade;

    @Column(name = "precisa_transporte", nullable = false)
    private boolean precisaTransporte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SituacaoReservaEntrega situacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Version
    @Column(nullable = false)
    private Long versao;

    @PrePersist
    private void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}

