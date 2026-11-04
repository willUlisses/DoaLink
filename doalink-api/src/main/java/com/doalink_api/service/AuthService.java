package com.doalink_api.service;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    private record DadosComunsTratados(TipoPessoa tipoPessoa, String documento, String email) { }


    @Transactional
    public Doador cadastrarDoador(CadastroComumRequest req) {
        DadosComunsTratados dadosTratados = prepararDadosComuns(req);
        Doador doador = new Doador();
        preencherComuns(doador, req.nome(), req.senha(), dadosTratados);
        return usuarioRepository.save(doador);
    }

    @Transactional
    public Transportador cadastrarTransportador(CadastroComumRequest req) {
        DadosComunsTratados dados = prepararDadosComuns(req);
        Transportador transportador = new Transportador();
        preencherComuns(transportador, req.nome(), req.senha(), dados);
        return usuarioRepository.save(transportador);
    }

    // TODO: criar método pra cadastrar o Receptor


    private DadosComunsTratados prepararDadosComuns(CadastroComumRequest req) {
        boolean temCpf = req.cpf() != null && !req.cpf().isBlank();

        TipoPessoa tipoPessoa = temCpf ? TipoPessoa.FISICA : TipoPessoa.JURIDICA;
        String documento = normalizarDocumento(temCpf ? req.cpf() : req.cnpj());
        String email = req.email().trim().toLowerCase();

        validarUnicidade(email, documento);

        // TODO: configurar cliente e consulta em api externa do cnpj

        return new DadosComunsTratados(tipoPessoa, documento, email);
    }


    private void preencherComuns(Usuario u, String nome, String senha, DadosComunsTratados d) {
        u.setTipoPessoa(d.tipoPessoa());
        u.setDocumento(d.documento());
        u.setNome(nome.trim());
        u.setEmail(d.email());
        // TODO: Configurar bean do encoder da senha e preencher ela criptografada.
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
