package com.doalink_api.controller;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.CadastroReceptorRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.response.UsuarioResponse;
import com.doalink_api.service.CookieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Endpoints de login, cadastro de perfis e renovação de token")
@RequestMapping("/auth")
public interface IAuthController {

    @Operation(
            summary = "Cadastrar usuário doador",
            description = "Realiza o cadastro de uma nova pessoa física ou jurídica com perfil de doador e efetua o login inicial gerando cookies de autenticação."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Doador cadastrado e autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado")
    })
    @PostMapping("/doadores/cadastro")
    ResponseEntity<UsuarioResponse> cadastrarDoador(
            @RequestBody @Valid CadastroComumRequest req,
            HttpServletResponse res);

    @Operation(
            summary = "Cadastrar usuário transportador",
            description = "Realiza o cadastro de um voluntário ou empresa transportadora e efetua o login inicial."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transportador cadastrado e autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado")
    })
    @PostMapping("/transportadores/cadastro")
    ResponseEntity<UsuarioResponse> cadastrarTransportador(
            @RequestBody @Valid CadastroComumRequest req,
            HttpServletResponse res);

    @Operation(
            summary = "Cadastrar usuário receptor (ONG / Entidade)",
            description = "Realiza o cadastro de entidade receptora de doações e efetua o login inicial."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Receptor cadastrado e autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado")
    })
    @PostMapping("/receptores/cadastro")
    ResponseEntity<UsuarioResponse> cadastrarReceptor(
            @RequestBody @Valid CadastroReceptorRequest req,
            HttpServletResponse res);

    @Operation(
            summary = "Autenticação de usuário",
            description = "Valida as credenciais do usuário (e-mail e senha) e retorna os dados do usuário, injetando access e refresh token nos cookies."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos")
    })
    @PostMapping("/login")
    ResponseEntity<UsuarioResponse> login(
            @RequestBody @Valid LoginRequest req,
            HttpServletResponse res);

    @Operation(
            summary = "Renovar token de acesso",
            description = "Gera um novo access token a partir do refresh token fornecido via cookie."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token renovado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado ou não informado")
    })
    @PostMapping("/refresh")
    ResponseEntity<UsuarioResponse> refresh(
            @CookieValue(name = CookieService.REFRESH, required = false) String refreshToken,
            HttpServletResponse res);

    @Operation(
            summary = "Logout de usuário",
            description = "Invalida a sessão/refresh token ativo e remove os cookies de autenticação do cliente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Sessão inválida ou não encontrada")
    })
    @PostMapping("/logout")
    ResponseEntity<Void> logout(
            @CookieValue(name = CookieService.REFRESH, required = false) String refreshToken,
            HttpServletResponse res);
}
