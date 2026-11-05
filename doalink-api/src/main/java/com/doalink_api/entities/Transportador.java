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
@Table(name = "transportadores")
public class Transportador extends Usuario {

    public Transportador() {
        setTipoUsuario(TipoUsuario.TRANSPORTADOR);
    }

    public Transportador(String documento, String nome, String email, String senha, TipoPessoa tipoPessoa) {
        super(documento, nome, email, senha, TipoUsuario.TRANSPORTADOR, tipoPessoa);
    }

}
