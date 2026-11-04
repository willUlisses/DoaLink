package com.doalink_api.dtos.response;

import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.TipoUsuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        TipoUsuario tipoUsuario
) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getTipoUsuario()
        );
    }

}
