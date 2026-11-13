package com.doalink_api.mapper;

import com.doalink_api.dtos.request.ItemNecessidadeRequest;
import com.doalink_api.dtos.request.NecessidadeDoacaoRequest;
import com.doalink_api.entities.ItemDoacao;
import com.doalink_api.entities.NecessidadeDoacao;
import com.doalink_api.entities.Receptor;

public final class NecessidadeDoacaoMapper {

    private NecessidadeDoacaoMapper() {}

    public static NecessidadeDoacao toEntity(NecessidadeDoacaoRequest req, Receptor receptor) {
        NecessidadeDoacao necessidade = new NecessidadeDoacao();
        necessidade.setReceptor(receptor);
        necessidade.setLocalEntrega(EnderecoMapper.toEndereco(req.localEntrega()));

        req.itens().stream()
                .map(NecessidadeDoacaoMapper::toItem)
                .forEach(necessidade::adicionarItem);

        return necessidade;
    }

    private static ItemDoacao toItem(ItemNecessidadeRequest req) {
        ItemDoacao item = new ItemDoacao();
        item.setDescricao(req.descricao() != null ? req.descricao().trim() : null);
        item.setTipoItemDoacao(req.tipoItemDoacao());
        item.setTipoUnidade(req.tipoUnidade());
        item.setQuantidade(req.quantidade());
        return item;
    }
}
