package com.doalink_api.entities;

import com.doalink_api.entities.enums.SituacaoNecessidade;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "necessidades_doacao")
public class NecessidadeDoacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receptor_id", nullable = false)
    private Receptor receptor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cep", column = @Column(name = "cep", nullable = false)),
            @AttributeOverride(name = "logradouro", column = @Column(name = "logradouro", nullable = false)),
            @AttributeOverride(name = "numero", column = @Column(name = "numero", nullable = false)),
            @AttributeOverride(name = "complemento", column = @Column(name = "complemento")),
            @AttributeOverride(name = "bairro", column = @Column(name = "bairro", nullable = false)),
            @AttributeOverride(name = "cidade", column = @Column(name = "cidade", nullable = false)),
            @AttributeOverride(name = "estado", column = @Column(name = "estado", nullable = false))
    })
    private Endereco localEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SituacaoNecessidade situacao = SituacaoNecessidade.ABERTA;

    @OneToMany(mappedBy = "necessidade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemDoacao> itens = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public void adicionarItem(ItemDoacao item) {
        item.setOferta(null);
        item.setNecessidade(this);
        itens.add(item);
    }

    @PrePersist
    private void prePersist() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = criadoEm;
    }

    @PreUpdate
    private void preUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
