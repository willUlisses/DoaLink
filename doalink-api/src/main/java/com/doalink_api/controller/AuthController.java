package com.doalink_api.controller;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.CadastroReceptorRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.request.ReenviarCodigoRequest;
import com.doalink_api.dtos.request.VerificarEmailRequest;
import com.doalink_api.dtos.response.AuthResponse;
import com.doalink_api.dtos.response.MensagemResponse;
import com.doalink_api.dtos.response.UsuarioResponse;
import com.doalink_api.service.AuthService;
import com.doalink_api.service.CookieService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements IAuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @Override
    public ResponseEntity<UsuarioResponse> cadastrarDoador(
            @RequestBody @Valid CadastroComumRequest req) {
        UsuarioResponse usuario = authService.cadastrarDoador(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @Override
    public ResponseEntity<UsuarioResponse> cadastrarTransportador(
            @RequestBody @Valid CadastroComumRequest req) {
        UsuarioResponse usuario = authService.cadastrarTransportador(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @Override
    public ResponseEntity<UsuarioResponse> cadastrarReceptor(
            @RequestBody @Valid CadastroReceptorRequest req) {
        UsuarioResponse usuario = authService.cadastrarReceptor(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @Override
    public ResponseEntity<UsuarioResponse> verificarEmail(
            @RequestBody @Valid VerificarEmailRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.verificarEmail(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.ok(auth.usuario());
    }

    @Override
    public ResponseEntity<MensagemResponse> reenviarCodigo(
            @RequestBody @Valid ReenviarCodigoRequest req) {
        authService.reenviarCodigoVerificacao(req);
        return ResponseEntity.ok(new MensagemResponse("Se o e-mail estiver cadastrado e pendente de verificação, um novo código foi enviado."));
    }

    @Override
    public ResponseEntity<UsuarioResponse> login(
            @RequestBody @Valid LoginRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.login(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.ok(auth.usuario());
    }

    @Override
    public ResponseEntity<UsuarioResponse> refresh(
            String refreshToken,
            HttpServletResponse res) {
        AuthResponse auth = authService.refresh(refreshToken);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.ok(auth.usuario());
    }

    @Override
    public ResponseEntity<Void> logout(
            String refreshToken,
            HttpServletResponse res) {
        authService.logout(refreshToken);
        cookieService.limpar(res);
        return ResponseEntity.noContent().build();
    }
}
