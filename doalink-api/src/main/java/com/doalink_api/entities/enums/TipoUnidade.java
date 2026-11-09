package com.doalink_api.entities.enums;

import lombok.Getter;

@Getter
public enum TipoUnidade {
    SEMUNIDADE("SemUnidade"),
    KG("Kilo"),
    T("Tonelada"),
    CAIXAS("Caixas"),
    CESTAS_BASICAS("CestasBasicas"),
    ML("MiliLitros"),
    G("Gramas");

   private final String Descrisao;

    TipoUnidade(String descrisao) {
        Descrisao = descrisao;
    }
}
