package com.doalink_api.controller;

import com.doalink_api.dtos.request.CampanhaEmergencialRequest;
import com.doalink_api.dtos.response.CampanhaEmergencialResponse;
import com.doalink_api.dtos.response.ErrorResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.entities.enums.SituacaoCampanha;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Campanhas Emergenciais", description = "Endpoints para gerenciamento de campanhas emergenciais por ONGs/Receptores")
@RequestMapping("/campanhas")
public interface ICampanhaEmergencialController {

    @Operation(summary = "Criar campanha emergencial", description = "Registra uma nova campanha emergencial com endereço de recebimento, descrição da causa e itens necessários. Restrito a ONGs (ROLE_RECEPTOR).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Campanha criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Apenas receptores podem criar campanhas",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    ResponseEntity<CampanhaEmergencialResponse> criar(
            @RequestBody @Valid CampanhaEmergencialRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario
    );

    @Operation(summary = "Listar minhas campanhas", description = "Retorna as campanhas registradas pelo receptor autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado para perfis não-receptores")
    })
    @GetMapping("/minhas")
    ResponseEntity<List<CampanhaEmergencialResponse>> listarMinhas(
            @AuthenticationPrincipal UsuarioAutenticado usuario
    );

    @Operation(summary = "Listar campanhas por situação", description = "Lista paginada de campanhas emergenciais. Se a situação não for informada, assume ATIVA.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    ResponseEntity<Page<CampanhaEmergencialResponse>> listar(
            @RequestParam(required = false) SituacaoCampanha situacao,
            Pageable pageable
    );

    @Operation(summary = "Buscar campanha por ID", description = "Retorna os detalhes de uma campanha emergencial e seus itens.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campanha encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    ResponseEntity<CampanhaEmergencialResponse> buscarPorId(@PathVariable Long id);
}
