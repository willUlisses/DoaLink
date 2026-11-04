package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoUsuario;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "doadores")
public class Doador extends Usuario {

    public Doador() {
        setTipoUsuario(TipoUsuario.DOADOR);
    }

}
