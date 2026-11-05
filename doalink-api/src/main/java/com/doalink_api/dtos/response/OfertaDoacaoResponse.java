package com.doalink_api.dtos.response;

import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Endereco;
import com.doalink_api.entities.ItemDoacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record OfertaDoacaoResponse(
        Long doadorId,
        EnderecoResponse endereco,
        List<ItemDoacaoResponse> itens
) {


    public static OfertaDoacaoResponse from(Doador doador, Endereco endereco, List<ItemDoacao> itens) {
        Long idDoDoador = null;
        if (doador != null) {
            idDoDoador = doador.getId();
        }

        EnderecoResponse enderecoResponse = null;
        if (endereco != null) {
            enderecoResponse = EnderecoResponse.from(endereco);
        }

        List<ItemDoacaoResponse> itensResponse = new ArrayList<>();
        if (itens != null) {
            for (ItemDoacao item : itens) {
                ItemDoacaoResponse itemResponse = ItemDoacaoResponse.from(item);
                itensResponse.add(itemResponse);
            }
        }

        return new OfertaDoacaoResponse(idDoDoador, enderecoResponse, itensResponse);
    }

    public static OfertaDoacaoResponse from(Long doadorId, Endereco endereco, List<ItemDoacao> itens) {
        EnderecoResponse enderecoResponse = null;
        if (endereco != null) {
            enderecoResponse = EnderecoResponse.from(endereco);
        }

        List<ItemDoacaoResponse> itensResponse = new ArrayList<>();
        if (itens != null) {
            for (ItemDoacao item : itens) {
                ItemDoacaoResponse itemResponse = ItemDoacaoResponse.from(item);
                itensResponse.add(itemResponse);
            }
        }

        return new OfertaDoacaoResponse(doadorId, enderecoResponse, itensResponse);
    }

    public static OfertaDoacaoResponse from(List<ItemDoacao> itens) {
        if (itens == null || itens.isEmpty()) {
            return new OfertaDoacaoResponse(null, null, Collections.emptyList());
        }

        ItemDoacao primeiroItem = itens.get(0);
        Doador doador = primeiroItem.getDoador();
        Endereco endereco = primeiroItem.getEndereco();

        return from(doador, endereco, itens);
    }

}
