package com.doalink_api.controller;

import com.doalink_api.dtos.request.DisponibilidadeTransporteRequest;
import com.doalink_api.dtos.response.DisponibilidadeTransporteResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Disponibilidades de Transporte", description = "Endpoints de cadastro, consulta pessoal e cancelamento de disponibilidades de transporte")
@RequestMapping("/disponibilidades")
public interface IDisponibilidadeTransporteController {

    @Operation(
            summary = "Cadastrar disponibilidade de transporte",
            description = "Registra uma nova disponibilidade de transporte vinculada ao usuário com perfil TRANSPORTADOR autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Disponibilidade criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DisponibilidadeTransporteResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Perfil TRANSPORTADOR obrigatório"),
            @ApiResponse(responseCode = "409", description = "Já existe disponibilidade ativa para esta data")
    })
    @PostMapping
    ResponseEntity<DisponibilidadeTransporteResponse> criar(
            @RequestBody @Valid DisponibilidadeTransporteRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(
            summary = "Listar minhas disponibilidades de transporte",
            description = "Retorna a listagem de todas as disponibilidades cadastradas pelo transportador autenticado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Perfil TRANSPORTADOR obrigatório")
    })
    @GetMapping("/minhas")
    ResponseEntity<List<DisponibilidadeTransporteResponse>> listarMinhas(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(
            summary = "Cancelar disponibilidade de transporte",
            description = "Cancela uma disponibilidade com situação DISPONIVEL. Somente o transportador proprietário pode cancelá-la."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Disponibilidade cancelada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado ou situação inválida para cancelamento"),
            @ApiResponse(responseCode = "404", description = "Disponibilidade não encontrada")
    })
    @PatchMapping("/{id}/cancelar")
    ResponseEntity<Void> cancelar(
            @Parameter(description = "ID da disponibilidade de transporte") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(
            summary = "Encerrar disponibilidade de transporte",
            description = "Marca uma disponibilidade RESERVADA sem entregas pendentes como CONCLUIDA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Disponibilidade encerrada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Disponibilidade não encontrada"),
            @ApiResponse(responseCode = "409", description = "Situação inválida ou existem entregas vinculadas pendentes")
    })
    @PatchMapping("/{id}/encerrar")
    ResponseEntity<Void> encerrar(
            @Parameter(description = "ID da disponibilidade de transporte") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);
}

