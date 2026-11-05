package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoItemDoacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "itens_doacao")
public class ItemDoacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double quantidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item_doacao", nullable = false)
    private TipoItemDoacao tipoItemDoacao;

    @Embedded
    private Endereco endereco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doador_id", nullable = false)
    private Doador doador;

    public ItemDoacao(Double quantidade, TipoItemDoacao tipoItemDoacao, Endereco endereco, Doador doador) {
        this.quantidade = quantidade;
        this.tipoItemDoacao = tipoItemDoacao;
        this.endereco = endereco;
        this.doador = doador;
    }

}
