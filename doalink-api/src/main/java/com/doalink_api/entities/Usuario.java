package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.entities.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String documento;

    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    /** Opcional — normalizado sem pontuação quando informado. */
    @Column
    private String telefone;

    /** Endereço opcional do usuário (ex.: endereço pessoal do doador). */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "cep",         column = @Column(name = "endereco_cep")),
        @AttributeOverride(name = "logradouro",  column = @Column(name = "endereco_logradouro")),
        @AttributeOverride(name = "numero",      column = @Column(name = "endereco_numero")),
        @AttributeOverride(name = "complemento", column = @Column(name = "endereco_complemento")),
        @AttributeOverride(name = "bairro",      column = @Column(name = "endereco_bairro")),
        @AttributeOverride(name = "cidade",      column = @Column(name = "endereco_cidade")),
        @AttributeOverride(name = "estado",      column = @Column(name = "endereco_estado"))
    })
    private Endereco endereco;

    @Setter(AccessLevel.PROTECTED)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoUsuario tipoUsuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPessoa tipoPessoa;

    public Usuario(String documento, String nome, String email, String senha,
                   String telefone, TipoUsuario tipoUsuario, TipoPessoa tipoPessoa) {
        this.documento = documento;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.tipoUsuario = tipoUsuario;
        this.tipoPessoa = tipoPessoa;
    }
}
