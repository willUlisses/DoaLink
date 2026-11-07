package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoItemDoacao;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
    @Column(length = 999)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item_doacao", nullable = false)
    private TipoItemDoacao tipoItemDoacao;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_id", nullable = false)
    private OfertaDoacao oferta;

    public ItemDoacao(String descricao, TipoItemDoacao tipoItemDoacao, BigDecimal quantidade) {
        this.descricao = descricao;
        this.tipoItemDoacao = tipoItemDoacao;
        this.quantidade = quantidade;
    }

}
