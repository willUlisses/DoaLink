package com.doalink_api.controller;

import com.doalink_api.dtos.request.NecessidadeDoacaoRequest;
import com.doalink_api.dtos.response.NecessidadeDoacaoResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.entities.enums.SituacaoNecessidade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Necessidades de Doação", description = "Endpoints de registro e consulta de necessidades de doação")
@RequestMapping("/necessidades")
public interface INecessidadeDoacaoController {

    @Operation(
            summary = "Registrar necessidade de doação",
            description = "Registra uma necessidade de doação vinculada ao usuário com perfil RECEPTOR autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Necessidade registrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NecessidadeDoacaoResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Perfil RECEPTOR obrigatório")
    })
    @PostMapping
    ResponseEntity<NecessidadeDoacaoResponse> criar(
            @RequestBody @Valid NecessidadeDoacaoRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(
            summary = "Listar minhas necessidades de doação",
            description = "Retorna as necessidades registradas pelo usuário RECEPTOR logado, da mais recente para a mais antiga."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Perfil RECEPTOR obrigatório")
    })
    @GetMapping("/minhas")
    ResponseEntity<List<NecessidadeDoacaoResponse>> listarMinhas(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(
            summary = "Listar necessidades por situação (consulta paginada)",
            description = "Retorna página de necessidades filtradas pela situação informada. Padrão: ABERTA."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de necessidades retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de filtro ou paginação inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    ResponseEntity<Page<NecessidadeDoacaoResponse>> listar(
            @Parameter(description = "Filtro opcional por situação (padrão: ABERTA)") @RequestParam(required = false) SituacaoNecessidade situacao,
            @ParameterObject @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable);

    @Operation(
            summary = "Buscar necessidade de doação por ID",
            description = "Retorna os detalhes completos de uma necessidade de doação pelo seu ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Necessidade encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NecessidadeDoacaoResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Necessidade não encontrada")
    })
    @GetMapping("/{id}")
    ResponseEntity<NecessidadeDoacaoResponse> buscarPorId(
            @Parameter(description = "ID da necessidade de doação") @PathVariable Long id);
}
