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
@Table(name = "itens_campanha")
public class ItemCampanha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campanha_id", nullable = false)
    private CampanhaEmergencial campanha;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item_doacao", nullable = false)
    private TipoItemDoacao tipoItemDoacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_unidade", nullable = false, length = 50)
    private TipoUnidade tipoUnidade;

    @Positive
    @Column(nullable = false)
    private Double quantidade;

    @Column(length = 500)
    private String descricao;

    public ItemCampanha(TipoItemDoacao tipoItemDoacao, TipoUnidade tipoUnidade, Double quantidade, String descricao) {
        this.tipoItemDoacao = tipoItemDoacao;
        this.tipoUnidade = tipoUnidade;
        this.quantidade = quantidade;
        this.descricao = descricao;
    }
}
