package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.entities.enums.TipoUsuario;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "doadores")
public class Doador extends Usuario {

    public Doador() {
        setTipoUsuario(TipoUsuario.DOADOR);
    }

    /** Usado pelo AuthService no cadastro (telefone opcional, passado como null se não informado). */
    public Doador(String documento, String nome, String email, String senha, TipoPessoa tipoPessoa) {
        super(documento, nome, email, senha, null, TipoUsuario.DOADOR, tipoPessoa);
    }

    /** Construtor completo com telefone. */
    public Doador(String documento, String nome, String email, String senha, String telefone, TipoPessoa tipoPessoa) {
        super(documento, nome, email, senha, telefone, TipoUsuario.DOADOR, tipoPessoa);
    }
}
