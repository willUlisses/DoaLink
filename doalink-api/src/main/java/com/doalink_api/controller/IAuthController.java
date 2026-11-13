package com.doalink_api.controller;

import com.doalink_api.dtos.request.CadastroComumRequest;
import com.doalink_api.dtos.request.CadastroReceptorRequest;
import com.doalink_api.dtos.request.LoginRequest;
import com.doalink_api.dtos.request.ReenviarCodigoRequest;
import com.doalink_api.dtos.request.VerificarEmailRequest;
import com.doalink_api.dtos.response.MensagemResponse;
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

@Tag(name = "Autenticação", description = "Endpoints de login, cadastro de perfis, confirmação de e-mail e renovação de token")
@RequestMapping("/auth")
public interface IAuthController {

    @Operation(
            summary = "Cadastrar usuário doador",
            description = "Realiza o cadastro de uma nova pessoa física ou jurídica com perfil de doador e dispara o código de verificação para o e-mail cadastrado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Doador cadastrado com sucesso. Código de confirmação enviado por e-mail.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado")
    })
    @PostMapping("/doadores/cadastro")
    ResponseEntity<UsuarioResponse> cadastrarDoador(
            @RequestBody @Valid CadastroComumRequest req);

    @Operation(
            summary = "Cadastrar usuário transportador",
            description = "Realiza o cadastro de um voluntário ou empresa transportadora e dispara o código de verificação para o e-mail cadastrado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transportador cadastrado com sucesso. Código de confirmação enviado por e-mail.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado")
    })
    @PostMapping("/transportadores/cadastro")
    ResponseEntity<UsuarioResponse> cadastrarTransportador(
            @RequestBody @Valid CadastroComumRequest req);

    @Operation(
            summary = "Cadastrar usuário receptor (ONG / Entidade)",
            description = "Realiza o cadastro de entidade receptora de doações e dispara o código de verificação para o e-mail cadastrado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Receptor cadastrado com sucesso. Código de confirmação enviado por e-mail.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail ou documento já cadastrado")
    })
    @PostMapping("/receptores/cadastro")
    ResponseEntity<UsuarioResponse> cadastrarReceptor(
            @RequestBody @Valid CadastroReceptorRequest req);

    @Operation(
            summary = "Verificar código de confirmação de e-mail",
            description = "Valida o código de 6 dígitos enviado por e-mail dentro da validade de 90 segundos. Ativa a conta e efetua login retornando os cookies de sessão."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "E-mail verificado e autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Código inválido, expirado ou formato incorreto"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping("/verificar-email")
    ResponseEntity<UsuarioResponse> verificarEmail(
            @RequestBody @Valid VerificarEmailRequest req,
            HttpServletResponse res);

    @Operation(
            summary = "Reenviar código de confirmação de e-mail",
            description = "Revoga códigos pendentes anteriores e gera e envia um novo código de 6 dígitos com validade de 90 segundos para o e-mail informado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Solicitação de reenvio processada",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos")
    })
    @PostMapping("/reenviar-codigo")
    ResponseEntity<MensagemResponse> reenviarCodigo(
            @RequestBody @Valid ReenviarCodigoRequest req);

    @Operation(
            summary = "Autenticação de usuário",
            description = "Valida as credenciais do usuário (e-mail e senha) e retorna os dados do usuário, injetando access e refresh token nos cookies. Exige que o e-mail esteja verificado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos"),
            @ApiResponse(responseCode = "403", description = "E-mail não verificado")
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
