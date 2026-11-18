package com.doalink_api.entities.enums;

import lombok.Getter;

@Getter
public enum TipoVeiculo {
    MOTO("Moto"),
    CARRO_PASSEIO("Carro de passeio"),
    UTILITARIO_LEVE("Utilitário leve"),
    CAMINHONETE("Caminhonete"),
    CAMINHAO_PEQUENO("Caminhão pequeno"),
    CAMINHAO_MEDIO("Caminhão médio"),
    CAMINHAO_GRANDE("Caminhão grande"),
    OUTRO("Outro");

    private final String descricao;

    TipoVeiculo(String descricao) {
        this.descricao = descricao;
    }
}

