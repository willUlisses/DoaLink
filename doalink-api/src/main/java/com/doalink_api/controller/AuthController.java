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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/doadores/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrarDoador(
            @RequestBody @Valid CadastroComumRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.cadastrarDoador(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.usuario());
    }

    @PostMapping("/transportadores/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrarTransportador(
            @RequestBody @Valid CadastroComumRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.cadastrarTransportador(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.usuario());
    }

    @PostMapping("/receptores/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrarReceptor(
            @RequestBody @Valid CadastroReceptorRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.cadastrarReceptor(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.usuario());
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(
            @RequestBody @Valid LoginRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.login(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.ok(auth.usuario());
    }

    @PostMapping("/refresh")
    public ResponseEntity<UsuarioResponse> refresh(
            @CookieValue(name = CookieService.REFRESH, required = false) String refreshToken,
            HttpServletResponse res) {
        AuthResponse auth = authService.refresh(refreshToken);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.ok(auth.usuario());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = CookieService.REFRESH, required = false) String refreshToken,
            HttpServletResponse res) {
        authService.logout(refreshToken);
        cookieService.limpar(res);
        return ResponseEntity.noContent().build();
    }
}
