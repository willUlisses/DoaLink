package com.doalink_api.controller;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.CadastroReceptorRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.response.AuthResponse;
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
            @RequestBody @Valid CadastroComumRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.cadastrarDoador(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.usuario());
    }

    @Override
    public ResponseEntity<UsuarioResponse> cadastrarTransportador(
            @RequestBody @Valid CadastroComumRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.cadastrarTransportador(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.usuario());
    }

    @Override
    public ResponseEntity<UsuarioResponse> cadastrarReceptor(
            @RequestBody @Valid CadastroReceptorRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.cadastrarReceptor(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.usuario());
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
