package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoEventoTransporte;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "eventos_transporte")
public class EventoTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private ReservaEntrega reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 60)
    private TipoEventoTransporte tipoEvento;

    @Column(nullable = false, length = 500)
    private String detalhes;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public EventoTransporte(ReservaEntrega reserva, Usuario usuario, TipoEventoTransporte tipoEvento, String detalhes) {
        this.reserva = reserva;
        this.usuario = usuario;
        this.tipoEvento = tipoEvento;
        this.detalhes = detalhes;
    }

    @PrePersist
    private void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }
}
