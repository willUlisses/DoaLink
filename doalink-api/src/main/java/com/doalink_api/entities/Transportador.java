package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoUsuario;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "transportadores")
public class Transportador extends Usuario {

    public Transportador() {
        setTipoUsuario(TipoUsuario.TRANSPORTADOR);
    }

}
