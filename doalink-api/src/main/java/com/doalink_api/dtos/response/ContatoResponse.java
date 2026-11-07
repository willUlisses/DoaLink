package com.doalink_api.dtos.response;

import com.doalink_api.entities.Usuario;

/*
response pra passar o contato de quem fez uma oferta ou de quem iniciou uma campanha
 */
public record ContatoResponse(
        String nome,
        String email,
        String telefone
) {
    public static ContatoResponse from(Usuario usuario) {
        return new ContatoResponse(
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getTelefone()
        );
    }
}
