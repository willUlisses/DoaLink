package com.doalink_api.entities.enums;

import lombok.Getter;

@Getter
public enum TipoPessoa {

    FISICA("FISICA"),
    JURIDICA("JURIDICA");

    private final String descricao;

    TipoPessoa(String descricao) {
        this.descricao = descricao;
    }




}
