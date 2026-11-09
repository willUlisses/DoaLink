package com.doalink_api.entities;

import com.doalink_api.entities.enums.SituacaoOferta;
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
@Table(name = "ofertas_doacao")
public class OfertaDoacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doador_id", nullable = false)
    private Doador doador;

    @Embedded
    private Endereco localRetirada;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoOferta situacao = SituacaoOferta.DISPONIVEL;

    @OneToMany(mappedBy = "oferta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemDoacao> itens = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    public OfertaDoacao(Doador doador, Endereco localRetirada) {
        this.doador = doador;
        this.localRetirada = localRetirada;
    }

    public void adicionarItem(ItemDoacao item) {
        item.setOferta(this);
        this.itens.add(item);
    }

    @PrePersist
    private void prePersist() {
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
