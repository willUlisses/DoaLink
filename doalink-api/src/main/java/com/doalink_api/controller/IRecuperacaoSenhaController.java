package com.doalink_api.controller;

import com.doalink_api.dtos.request.RedefinirSenhaRequest;
import com.doalink_api.dtos.request.SolicitarRecuperacaoSenhaRequest;
import com.doalink_api.dtos.response.MensagemResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Recuperação de Senha", description = "Endpoints para recuperação e redefinição de senha de usuários")
@RequestMapping("/auth")
public interface IRecuperacaoSenhaController {

    @Operation(
            summary = "Solicitar recuperação de senha",
            description = "Envia um link de recuperação para o e-mail cadastrado. Retorna resposta uniforme (200 OK) para mitigar enumeração de contas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Solicitação processada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "E-mail inválido ou malformatado")
    })
    @PostMapping("/recuperar-senha")
    ResponseEntity<MensagemResponse> solicitarRecuperacao(
            @Valid @RequestBody SolicitarRecuperacaoSenhaRequest request);

    @Operation(
            summary = "Validar token de recuperação",
            description = "Verifica se o token informado existe, ainda não expirou (validade de 10 minutos) e não foi utilizado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Status de validação do token retornado com sucesso"
            ),
            @ApiResponse(responseCode = "400", description = "Token não informado ou em branco")
    })
    @GetMapping("/recuperar-senha/validar")
    ResponseEntity<Map<String, Object>> validarToken(
            @Parameter(description = "Token opaco recebido por e-mail", required = true)
            @RequestParam @NotBlank(message = "O token é obrigatório") String token);

    @Operation(
            summary = "Redefinir senha",
            description = "Atualiza a senha do usuário utilizando um token de recuperação válido, invalidando-o e revogando todas as sessões ativas."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Senha redefinida com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MensagemResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos (senha fora dos padrões ou campos vazios)"),
            @ApiResponse(responseCode = "404", description = "Token de recuperação inválido, expirado ou já utilizado")
    })
    @PostMapping("/redefinir-senha")
    ResponseEntity<MensagemResponse> redefinirSenha(
            @Valid @RequestBody RedefinirSenhaRequest request);
}
