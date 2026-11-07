package com.doalink_api.mapper;

import com.doalink_api.dtos.request.OfertaDoacaoRequest;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Endereco;
import com.doalink_api.entities.OfertaDoacao;

public class OfertaDoacaoMapper {

    private OfertaDoacaoMapper() {}

    public static OfertaDoacao toEntity(OfertaDoacaoRequest req, Doador doador) {
        Endereco localRetirada = req.endereco() != null
                ? EnderecoMapper.toEndereco(req.endereco())
                : null;

        OfertaDoacao oferta = new OfertaDoacao(doador, localRetirada);

        req.itens().stream()
                .map(ItemDoacaoMapper::toEntity)
                .forEach(oferta::adicionarItem);

        return oferta;
    }
}
