package com.doalink_api.entities.enums;

public enum TipoUsuario {

    DOADOR("DOADOR"),
    TRANSPORTADOR("TRANSPORTADOR"),
    RECEPTOR("RECEPTOR");

    private final String valor;

    TipoUsuario(String valor) {
        this.valor = valor;
    }
}
