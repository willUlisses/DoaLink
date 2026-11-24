package com.doalink_api.dtos.response;

import com.doalink_api.entities.Endereco;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.SituacaoTransportador;
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
        SituacaoTransportador situacaoConta,
        String motivoSuspensao,
        EnderecoResponse endereco
) {
    public UsuarioDetalheResponse(
            Long id,
            String nome,
            String documento,
            String email,
            String emailContato,
            String telefone,
            TipoUsuario tipoUsuario,
            TipoPessoa tipoPessoa,
            boolean emailVerificado,
            EnderecoResponse endereco) {
        this(id, nome, documento, email, emailContato, telefone, tipoUsuario, tipoPessoa, emailVerificado, null, null, endereco);
    }

    public static UsuarioDetalheResponse from(Usuario u) {
        Endereco endereco = u instanceof Receptor receptor
                ? (receptor.getEnderecoOrganizacao() != null ? receptor.getEnderecoOrganizacao() : receptor.getEndereco())
                : u.getEndereco();
        SituacaoTransportador situacaoConta = u instanceof Transportador t ? t.getSituacao() : null;
        String motivoSuspensao = u instanceof Transportador t ? t.getMotivoSuspensao() : null;
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
                situacaoConta,
                motivoSuspensao,
                EnderecoResponse.from(endereco)
        );
    }
}
