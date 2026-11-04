package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoUsuario;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "receptores")
public class Receptor extends Usuario {

    @Embedded
    private Endereco endereco;

    public Receptor() {
        setTipoUsuario(TipoUsuario.RECEPTOR);
    }

}
