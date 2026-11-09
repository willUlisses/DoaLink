package com.doalink_api.controller;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.CadastroReceptorRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.response.AuthResponse;
import com.doalink_api.dtos.response.UsuarioResponse;
import com.doalink_api.service.AuthService;
import com.doalink_api.service.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Endpoints de login, cadastro de perfis e renovação de token")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    @Operation(
            summary = "Cadastrar usuário doador",
            description = "Realiza o cadastro de uma nova pessoa física ou jurídica com perfil de doador e efetua o login inicial gerando cookies de autenticação."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Doador cadastrado e autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado")
    })
    @PostMapping("/doadores/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrarDoador(
            @RequestBody @Valid CadastroComumRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.cadastrarDoador(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.usuario());
    }

    @Operation(
            summary = "Cadastrar usuário transportador",
            description = "Realiza o cadastro de um voluntário ou empresa transportadora e efetua o login inicial."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Transportador cadastrado e autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado")
    })
    @PostMapping("/transportadores/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrarTransportador(
            @RequestBody @Valid CadastroComumRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.cadastrarTransportador(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.usuario());
    }

    @Operation(
            summary = "Cadastrar usuário receptor (ONG / Entidade)",
            description = "Realiza o cadastro de entidade receptora de doações e efetua o login inicial."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Receptor cadastrado e autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado")
    })
    @PostMapping("/receptores/cadastro")
    public ResponseEntity<UsuarioResponse> cadastrarReceptor(
            @RequestBody @Valid CadastroReceptorRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.cadastrarReceptor(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.usuario());
    }

    @Operation(
            summary = "Autenticação de usuário",
            description = "Valida as credenciais do usuário (e-mail e senha) e retorna os dados do usuário, injetando access e refresh token nos cookies."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(
            @RequestBody @Valid LoginRequest req,
            HttpServletResponse res) {
        AuthResponse auth = authService.login(req);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.ok(auth.usuario());
    }

    @Operation(
            summary = "Renovar token de acesso",
            description = "Gera um novo access token a partir do refresh token fornecido via cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token renovado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado ou não informado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<UsuarioResponse> refresh(
            @CookieValue(name = CookieService.REFRESH, required = false) String refreshToken,
            HttpServletResponse res) {
        AuthResponse auth = authService.refresh(refreshToken);
        cookieService.adicionarHeaders(res, auth.accessToken(), auth.refreshToken());
        return ResponseEntity.ok(auth.usuario());
    }

    @Operation(
            summary = "Logout de usuário",
            description = "Invalida a sessão/refresh token ativo e remove os cookies de autenticação do cliente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Sessão inválida ou não encontrada")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = CookieService.REFRESH, required = false) String refreshToken,
            HttpServletResponse res) {
        authService.logout(refreshToken);
        cookieService.limpar(res);
        return ResponseEntity.noContent().build();
    }
}
