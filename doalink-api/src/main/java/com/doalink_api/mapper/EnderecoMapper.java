package com.doalink_api.mapper;

import com.doalink_api.dtos.request.EnderecoRequest;
import com.doalink_api.entities.Endereco;

public class EnderecoMapper {

    public static Endereco toEndereco(EnderecoRequest er) {
        Endereco e = new Endereco();
        e.setLogradouro(er.logradouro());
        e.setNumero(er.numero());
        e.setComplemento(er.complemento());
        e.setBairro(er.bairro());
        e.setCidade(er.cidade());
        e.setEstado(er.estado().toUpperCase());
        e.setCep(er.cep().replaceAll("\\D", ""));
        return e;
    }

    public static EnderecoRequest toRequest(Endereco e) {
        return new EnderecoRequest(
                e.getLogradouro(),
                e.getNumero(),
                e.getComplemento(),
                e.getBairro(),
                e.getCidade(),
                e.getEstado(),
                e.getCep()
        );
    }

}
