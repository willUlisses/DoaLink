package com.doalink_api.service;

import com.doalink_api.config.UsuarioDetails;
import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.CadastroReceptorRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.response.AuthResponse;
import com.doalink_api.dtos.response.UsuarioResponse;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.TokenInvalidoException;
import com.doalink_api.mapper.EnderecoMapper;
import com.doalink_api.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder  passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse cadastrarDoador(CadastroComumRequest req) {
        return processarCadastroDoadorOuTransportador(new Doador(), req);
    }

    @Transactional
    public AuthResponse cadastrarTransportador(CadastroComumRequest req) {
        return processarCadastroDoadorOuTransportador(new Transportador(), req);
    }

    @Transactional
    public AuthResponse cadastrarReceptor(CadastroReceptorRequest req) {
        String documento = normalizarDocumento(req.cnpj());
        String email = req.email().trim().toLowerCase();

        validarUnicidade(email, documento);
        // TODO: Configurar cliente pra chamar api de validar cnpj

        Receptor receptor = new Receptor();
        receptor.setEnderecoOrganizacao(EnderecoMapper.toEndereco(req.endereco()));
        preencherComuns(receptor, req.nome(), req.senha(), TipoPessoa.JURIDICA, documento, email);

        return autenticarEGerarTokens(usuarioRepository.save(receptor));
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.email().trim().toLowerCase(), req.senha()));
        Usuario usuario = ((UsuarioDetails) Objects.requireNonNull(auth.getPrincipal())).getUsuario();
        return autenticarEGerarTokens(usuario);
    }

    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null) throw new TokenInvalidoException("Refresh token nulo");

        RefreshTokenService.Rotacao r = refreshTokenService.rotacionar(refreshToken);
        return new AuthResponse(UsuarioResponse.from(r.usuario()),
                jwtService.gerarAccessToken(r.usuario()), r.novoRefreshToken());
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) refreshTokenService.revogar(refreshToken);
    }

    private AuthResponse processarCadastroDoadorOuTransportador(Usuario usuario, CadastroComumRequest req) {
        String documento = normalizarDocumento(req.getDocumentoPreenchido());
        String email = req.email().trim().toLowerCase();

        validarUnicidade(email, documento);
        // TODO: se req for pessoa jurídica, chamar api do cnpj

        preencherComuns(usuario, req.nome(), req.senha(), req.getTipoPessoa(), documento, email);
        return autenticarEGerarTokens(usuarioRepository.save(usuario));
    }

    private AuthResponse autenticarEGerarTokens(Usuario u) {
        return new AuthResponse(UsuarioResponse.from(u),
                jwtService.gerarAccessToken(u), refreshTokenService.criar(u));
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
