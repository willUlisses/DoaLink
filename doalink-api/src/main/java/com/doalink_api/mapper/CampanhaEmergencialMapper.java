package com.doalink_api.mapper;

import com.doalink_api.dtos.request.CampanhaEmergencialRequest;
import com.doalink_api.dtos.request.ItemCampanhaRequest;
import com.doalink_api.entities.CampanhaEmergencial;
import com.doalink_api.entities.ItemCampanha;
import com.doalink_api.entities.Receptor;

public class CampanhaEmergencialMapper {

    private CampanhaEmergencialMapper() {}

    public static CampanhaEmergencial toEntity(CampanhaEmergencialRequest req, Receptor receptor) {
        CampanhaEmergencial campanha = new CampanhaEmergencial();
        campanha.setReceptor(receptor);
        campanha.setNome(req.nome());
        campanha.setDescricao(req.descricao());
        campanha.setEnderecoRecebimento(EnderecoMapper.toEndereco(req.enderecoRecebimento()));

        if (req.itens() != null) {
            for (ItemCampanhaRequest itemReq : req.itens()) {
                ItemCampanha item = new ItemCampanha(
                        itemReq.tipoItemDoacao(),
                        itemReq.tipoUnidade(),
                        itemReq.quantidade(),
                        itemReq.descricao()
                );
                campanha.adicionarItem(item);
            }
        }

        return campanha;
    }
}
