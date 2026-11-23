package com.doalink_api.dtos.response;

import com.doalink_api.entities.Endereco;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.entities.enums.TipoUsuario;

public record UsuarioDetalheResponse(
        Long id,
        String nome,
        String documento,
        String email,
        String emailContato,
        String telefone,
        TipoUsuario tipoUsuario,
        TipoPessoa tipoPessoa,
        boolean emailVerificado,
        EnderecoResponse endereco
) {
    public static UsuarioDetalheResponse from(Usuario u) {
        Endereco endereco = u instanceof Receptor receptor
                ? (receptor.getEnderecoOrganizacao() != null ? receptor.getEnderecoOrganizacao() : receptor.getEndereco())
                : u.getEndereco();
        return new UsuarioDetalheResponse(
                u.getId(),
                u.getNome(),
                u.getDocumento(),
                u.getEmail(),
                u.getEmailContatoEfetivo(),
                u.getTelefone(),
                u.getTipoUsuario(),
                u.getTipoPessoa(),
                u.isEmailVerificado(),
                EnderecoResponse.from(endereco)
        );
    }
}
