package com.doalink_api.dtos.response;

import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Endereco;
import com.doalink_api.entities.ItemDoacao;

import java.util.List;

public record OfertaDoacaoResponse(
        Long doadorId,
        EnderecoResponse endereco,
        List<ItemDoacaoResponse> itens) {

    public static OfertaDoacaoResponse from(Doador doador, Endereco endereco, List<ItemDoacao> itens) {
        return new OfertaDoacaoResponse(
                doador != null ? doador.getId() : null,
                endereco != null ? EnderecoResponse.from(endereco) : null,
                itens != null ? itens.stream().map(ItemDoacaoResponse::from).toList() : List.of()
        );
    }

    public static OfertaDoacaoResponse from(Long doadorId, Endereco endereco, List<ItemDoacao> itens) {
        return new OfertaDoacaoResponse(
                doadorId,
                endereco != null ? EnderecoResponse.from(endereco) : null,
                itens != null ? itens.stream().map(ItemDoacaoResponse::from).toList() : List.of()
        );
    }

    public static OfertaDoacaoResponse from(List<ItemDoacao> itens) {
        if (itens == null || itens.isEmpty()) {
            return new OfertaDoacaoResponse(null, null, List.of());
        }

        ItemDoacao primeiroItem = itens.get(0);
        Doador doador = primeiroItem.getDoador();
        Endereco endereco = primeiroItem.getEndereco();

        return from(doador, endereco, itens);
    }

}
