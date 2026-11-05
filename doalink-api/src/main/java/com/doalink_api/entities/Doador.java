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

    public Doador(String documento, String nome, String email, String senha, TipoPessoa tipoPessoa) {
        super(documento, nome, email, senha, TipoUsuario.DOADOR, tipoPessoa);
    }

}
