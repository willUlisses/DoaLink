package com.doalink_api.entities;

import com.doalink_api.entities.enums.SituacaoOcorrenciaTransporte;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ocorrencias_transporte")
public class OcorrenciaTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false, unique = true)
    private ReservaEntrega reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaDoacao oferta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transportador_id", nullable = false)
    private Transportador transportador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doador_id", nullable = false)
    private Doador doador;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receptor_id", nullable = false)
    private Receptor receptor;

    @Column(nullable = false, length = 120)
    private String motivo;

    @Column(nullable = false, length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private SituacaoOcorrenciaTransporte situacao = SituacaoOcorrenciaTransporte.PENDENTE_INVESTIGACAO;

    @Column(name = "aceito_em")
    private LocalDateTime aceitoEm;

    @Column(name = "autorizado_em")
    private LocalDateTime autorizadoEm;

    @Column(name = "retirada_confirmada_em")
    private LocalDateTime retiradaConfirmadaEm;

    @Column(name = "prazo_entrega_expirado_em", nullable = false)
    private LocalDateTime prazoEntregaExpiradoEm;

    @Column(name = "tentativas_validacao_entrega", nullable = false)
    private int tentativasValidacaoEntrega = 0;

    @Column(name = "observacoes_resolucao", length = 1000)
    private String observacoesResolucao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "resolvido_em")
    private LocalDateTime resolvidoEm;

    @PrePersist
    private void prePersist() {
        LocalDateTime agora = LocalDateTime.now();
        if (this.criadoEm == null) {
            this.criadoEm = agora;
        }
        this.atualizadoEm = agora;
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
