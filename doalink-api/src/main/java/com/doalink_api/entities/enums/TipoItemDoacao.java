package com.doalink_api.entities.enums;

import lombok.Getter;

@Getter
public enum TipoItemDoacao {
    PERECIVEL("Prefecivel"),
    NAO_PERECIVEL("NaoPerecivel"),
    HIGIENE("Higiene"),
    ROUPAS_E_TEXTIL("RoupasETextil"),
    MEDICAMENTOS("Medicamentos"),
    ELETRODOMESTICOS("Eletrodomesticos");

    private final String Descrisao;

    TipoUnidade(String descrisao) {
        Descrisao = descrisao;
    }

}
