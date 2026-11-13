package com.doalink_api.service;

import com.doalink_api.config.UsuarioDetails;
import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.CadastroReceptorRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.request.ReenviarCodigoRequest;
import com.doalink_api.dtos.request.VerificarEmailRequest;
import com.doalink_api.dtos.response.AuthResponse;
import com.doalink_api.dtos.response.UsuarioResponse;
import com.doalink_api.entities.Doador;
import com.doalink_api.entities.Receptor;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.Usuario;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.exception.ConflictException;
import com.doalink_api.exception.EmailNaoVerificadoException;
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
    private final VerificacaoEmailService verificacaoEmailService;

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
        receptor.setEnderecoOrganizacao(EnderecoMapper.toEndereco(req.endereco()));
        preencherComuns(receptor, req.nome(), req.senha(), TipoPessoa.JURIDICA, documento, email);

        Usuario salvo = usuarioRepository.save(receptor);
        verificacaoEmailService.enviarCodigo(salvo);
        return UsuarioResponse.from(salvo);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        req.email().trim().toLowerCase(), req.senha()));
        Usuario usuario = ((UsuarioDetails) Objects.requireNonNull(auth.getPrincipal())).getUsuario();
        if (!usuario.isEmailVerificado()) {
            throw new EmailNaoVerificadoException("E-mail não verificado. Confirme o código enviado para o seu e-mail antes de acessar a conta.");
        }
        return autenticarEGerarTokens(usuario);
    }

    @Transactional
    public AuthResponse verificarEmail(VerificarEmailRequest req) {
        Usuario usuario = verificacaoEmailService.verificarCodigo(req.email(), req.codigo());
        return autenticarEGerarTokens(usuario);
    }

    @Transactional
    public void reenviarCodigoVerificacao(ReenviarCodigoRequest req) {
        verificacaoEmailService.reenviarCodigo(req.email());
    }

    public AuthResponse refresh(String refreshToken) {
        if (refreshToken == null) throw new TokenInvalidoException("Refresh token nulo");

        RefreshTokenService.Rotacao r = refreshTokenService.rotacionar(refreshToken);
        if (!r.usuario().isEmailVerificado()) {
            throw new EmailNaoVerificadoException("E-mail não verificado. Confirme o código enviado para o seu e-mail antes de acessar a conta.");
        }
        return new AuthResponse(UsuarioResponse.from(r.usuario()),
                jwtService.gerarAccessToken(r.usuario()), r.novoRefreshToken());
    }

    public void logout(String refreshToken) {
        if (refreshToken != null) refreshTokenService.revogar(refreshToken);
    }

    private UsuarioResponse processarCadastroDoadorOuTransportador(Usuario usuario, CadastroComumRequest req) {
        String documento = normalizarDocumento(req.getDocumentoPreenchido());
        String email = req.email().trim().toLowerCase();

        validarUnicidade(email, documento);
        // TODO: se req for pessoa jurídica, chamar api do cnpj

        preencherComuns(usuario, req.nome(), req.senha(), req.getTipoPessoa(), documento, email);
        Usuario salvo = usuarioRepository.save(usuario);
        verificacaoEmailService.enviarCodigo(salvo);
        return UsuarioResponse.from(salvo);
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
