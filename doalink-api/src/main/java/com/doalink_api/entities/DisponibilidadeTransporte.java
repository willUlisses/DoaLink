package com.doalink_api.entities;

import com.doalink_api.entities.enums.SituacaoDisponibilidade;
import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.entities.enums.TipoVeiculo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "disponibilidades_transporte")
public class DisponibilidadeTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportador_id", nullable = false)
    private Transportador transportador;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_veiculo", nullable = false, length = 50)
    private TipoVeiculo tipoVeiculo;

    @Column(nullable = false)
    private Double capacidade;

    @Column(name = "capacidade_restante", nullable = false)
    private Double capacidadeRestante;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_unidade", nullable = false, length = 50)
    private TipoUnidade tipoUnidade;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cep", column = @Column(name = "origem_cep", nullable = false)),
            @AttributeOverride(name = "logradouro", column = @Column(name = "origem_logradouro", nullable = false)),
            @AttributeOverride(name = "numero", column = @Column(name = "origem_numero", nullable = false)),
            @AttributeOverride(name = "complemento", column = @Column(name = "origem_complemento")),
            @AttributeOverride(name = "bairro", column = @Column(name = "origem_bairro", nullable = false)),
            @AttributeOverride(name = "cidade", column = @Column(name = "origem_cidade", nullable = false)),
            @AttributeOverride(name = "estado", column = @Column(name = "origem_estado", nullable = false))
    })
    private Endereco origem;

    @Column(name = "data_disponivel", nullable = false)
    private LocalDate dataDisponivel;

    @Column(name = "horario_disponivel")
    private LocalTime horarioDisponivel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoDisponibilidade situacao = SituacaoDisponibilidade.DISPONIVEL;

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

