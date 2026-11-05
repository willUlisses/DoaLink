package com.doalink_api.service;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.CadastroReceptorRequest;
import com.doalink_api.dtos.response.UsuarioResponse;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.mapper.EnderecoMapper;
import com.doalink_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder  passwordEncoder;

    @Transactional
    public UsuarioResponse cadastrarDoador(CadastroComumRequest req) {
        return processarCadastroDoadorOuTransportador(new Doador(), req);
    }

    @Transactional
    public UsuarioResponse cadastrarTransportador(CadastroComumRequest req) {
        return processarCadastroDoadorOuTransportador(new Transportador(), req);
    }

    @Transactional
    public UsuarioResponse cadastrarReceptor(CadastroReceptorRequest req) {
        String documento = normalizarDocumento(req.cnpj());
        String email = req.email().trim().toLowerCase();

        validarUnicidade(email, documento);
        // TODO: Configurar cliente pra chamar api de validar cnpj

        Receptor receptor = new Receptor();
        receptor.setEndereco(EnderecoMapper.toEndereco(req.endereco()));
        preencherComuns(receptor, req.nome(), req.senha(), TipoPessoa.JURIDICA, documento, email);

        return UsuarioResponse.from(usuarioRepository.save(receptor));
    }




    private UsuarioResponse processarCadastroDoadorOuTransportador(Usuario usuario, CadastroComumRequest req) {
        String documento = normalizarDocumento(req.getDocumentoPreenchido());
        String email = req.email().trim().toLowerCase();
        // TODO: se req for pessoa jurídica, chamar api do cnpj

        validarUnicidade(email, documento);
        preencherComuns(usuario, req.nome(), req.senha(), req.getTipoPessoa(), documento, email);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    private void preencherComuns(Usuario u, String nome, String senha, TipoPessoa tipoPessoa, String doc, String email) {
        u.setTipoPessoa(tipoPessoa);
        u.setDocumento(doc);
        u.setNome(nome.trim());
        u.setEmail(email);
        u.setSenha(passwordEncoder.encode(senha));
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
