package com.doalink_api.controller;

import com.doalink_api.dtos.request.OfertaDoacaoRequest;
import com.doalink_api.dtos.response.OfertaDoacaoResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.service.OfertaDoacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Tag(name = "Ofertas de Doação", description = "Endpoints de gerenciamento e listagem de ofertas de doação")
@RestController
@RequestMapping("/ofertas")
@RequiredArgsConstructor
public class OfertaDoacaoController {

    private final OfertaDoacaoService ofertaService;

    @Operation(
            summary = "Criar nova oferta de doação",
            description = "Cria uma nova oferta de doação vinculada ao usuário com perfil DOADOR autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Oferta criada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaDoacaoResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Perfil DOADOR obrigatório")
    })
    @PostMapping
    public ResponseEntity<OfertaDoacaoResponse> criar(
            @RequestBody @Valid OfertaDoacaoRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {

        OfertaDoacaoResponse resp = ofertaService.criar(usuario.id(), req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resp.id()).toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @Operation(
            summary = "Listar minhas ofertas de doação",
            description = "Retorna a listagem de todas as ofertas cadastradas pelo usuário DOADOR logado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Perfil DOADOR obrigatório")
    })
    @GetMapping("/minhas")
    public ResponseEntity<List<OfertaDoacaoResponse>> listarMinhas(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(ofertaService.listarMinhas(usuario.id()));
    }

    @Operation(
            summary = "Listar ofertas disponíveis (consulta pública paginada)",
            description = "Retorna página de ofertas com situação DISPONIVEL, com filtros opcionais por categoria, cidade e estado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de ofertas retornada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Parâmetros de filtro ou paginação inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @GetMapping
    public ResponseEntity<Page<OfertaDoacaoResponse>> listarDisponiveis(
            @ParameterObject @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable,
            @Parameter(description = "Filtro opcional por tipo de item") @RequestParam(required = false) TipoItemDoacao categoria,
            @Parameter(description = "Filtro opcional por cidade") @RequestParam(required = false) String cidade,
            @Parameter(description = "Filtro opcional por UF do estado (ex: SP)") @RequestParam(required = false) String estado) {

        return ResponseEntity.ok(ofertaService.listarDisponiveis(pageable, categoria, cidade, estado));
    }

    @Operation(
            summary = "Buscar oferta de doação por ID",
            description = "Retorna os detalhes completos de uma oferta de doação pelo seu ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Oferta encontrada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OfertaDoacaoResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OfertaDoacaoResponse> buscarPorId(
            @Parameter(description = "ID da oferta de doação") @PathVariable Long id) {
        return ResponseEntity.ok(ofertaService.buscarPorId(id));
    }

    @Operation(
            summary = "Cancelar oferta de doação",
            description = "Cancela uma oferta de doação previamente criada. Somente o doador proprietário da oferta pode cancelá-la."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Oferta cancelada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Oferta não pode ser cancelada no status atual"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado: Somente o dono da oferta pode cancelá-la"),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada")
    })
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @Parameter(description = "ID da oferta de doação a ser cancelada") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {

        ofertaService.cancelar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
