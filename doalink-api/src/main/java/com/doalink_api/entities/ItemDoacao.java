package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoItemDoacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ItemDoacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double quantidade;

    @Enumerated(EnumType.STRING)
    @Column
    private TipoItemDoacao tipoItemDoacao;


    @Embedded
    private Endereco endereco;

    public ItemDoacao(Double quantidade, TipoItemDoacao tipoItemDoacao, Endereco endereco) {
        this.quantidade = quantidade;
        this.tipoItemDoacao = tipoItemDoacao;
        this.endereco = endereco;
    }

}
