package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.entities.enums.TipoUnidade;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "itens_doacao")
public class ItemDoacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //coloquei para na hora de adicionar um item poder ter uma descrisão do oq é o item
    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item_doacao", nullable = false)
    private TipoItemDoacao tipoItemDoacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_unidade", nullable = false, length = 50)
    private TipoUnidade tipoUnidade;

    @Positive
    @Column(nullable = false)
    private Double quantidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id")
    private OfertaDoacao oferta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "necessidade_id")
    private NecessidadeDoacao necessidade;

    public ItemDoacao(String descricao, TipoItemDoacao tipoItemDoacao, Double quantidade) {
        this.descricao = descricao;
        this.tipoItemDoacao = tipoItemDoacao;
        this.quantidade = quantidade;
    }

}
