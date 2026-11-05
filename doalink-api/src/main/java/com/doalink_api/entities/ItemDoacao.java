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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public TipoItemDoacao getTipoItemDoacao() {
        return tipoItemDoacao;
    }

    public void setTipoItemDoacao(TipoItemDoacao tipoItemDoacao) {
        this.tipoItemDoacao = tipoItemDoacao;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    public Doador getDoador() {
        return doador;
    }

    public void setDoador(Doador doador) {
        this.doador = doador;
    }

}
