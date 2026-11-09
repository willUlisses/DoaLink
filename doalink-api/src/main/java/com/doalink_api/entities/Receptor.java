package com.doalink_api.entities;

import com.doalink_api.entities.enums.TipoUsuario;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "receptores")
public class Receptor extends Usuario {

    // Endereço obrigatório do receptor (sede/organização), separado do endereço
    // genérico herdado de Usuario (que fica nas colunas padrão da tabela usuarios).
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "cep",         column = @Column(name = "receptor_cep")),
        @AttributeOverride(name = "logradouro",  column = @Column(name = "receptor_logradouro")),
        @AttributeOverride(name = "numero",      column = @Column(name = "receptor_numero")),
        @AttributeOverride(name = "complemento", column = @Column(name = "receptor_complemento")),
        @AttributeOverride(name = "bairro",      column = @Column(name = "receptor_bairro")),
        @AttributeOverride(name = "cidade",      column = @Column(name = "receptor_cidade")),
        @AttributeOverride(name = "estado",      column = @Column(name = "receptor_estado"))
    })
    private Endereco enderecoOrganizacao;

    public Receptor() {
        setTipoUsuario(TipoUsuario.RECEPTOR);
    }

}
