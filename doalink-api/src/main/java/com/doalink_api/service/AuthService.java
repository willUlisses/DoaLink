package com.doalink_api.utils;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.DadosComuns;
import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CadastroUtils {

    private final UsuarioRepository usuarioRepository;

    public DadosComuns prepararComum(CadastroComumRequest req) {
        boolean temCpf = req.cpf() != null && !req.cpf().isBlank();

        TipoPessoa tipoPessoa = temCpf ? TipoPessoa.FISICA : TipoPessoa.JURIDICA;
        String documento = normalizarDocumento(temCpf ? req.cpf() : req.cnpj());
        String email = req.email().trim().toLowerCase();

        validarUnicidade(email, documento);

        // TODO: configurar consulta em api externa do cnpj

        return new DadosComuns(tipoPessoa, documento, email);
    }


    public void preencherComuns(Usuario u, String nome, String senha, DadosComuns d) {
        u.setTipoPessoa(d.tipoPessoa());
        u.setDocumento(d.documento());
        u.setNome(nome.trim());
        u.setEmail(d.email());
        // TODO: Configurar Bean do encoder da senha e preencher ela criptografada.
    }


    private void validarUnicidade(String email, String documento) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictException("E-mail já cadastrado");
        }
        if (usuarioRepository.existsByDocumento(documento)) {
            throw new ConflictException("Documento já cadastrado");
        }
    }

    private String normalizarDocumento(String doc) {
        return doc.replaceAll("[^0-9A-Za-z]", "").toUpperCase();
    }


}
