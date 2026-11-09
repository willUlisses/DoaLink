package com.doalink_api.controller;

import com.doalink_api.dtos.request.OfertaDoacaoRequest;
import com.doalink_api.dtos.response.OfertaDoacaoResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.entities.enums.TipoItemDoacao;
import com.doalink_api.service.OfertaDoacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/ofertas")
@RequiredArgsConstructor
public class OfertaDoacaoController {

    private final OfertaDoacaoService ofertaService;

    /**
     * POST /ofertas — cria oferta (DOADOR)
     * Retorna 201 + Location: /ofertas/{id}
     */
    @PostMapping
    public ResponseEntity<OfertaDoacaoResponse> criar(
            @RequestBody @Valid OfertaDoacaoRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        OfertaDoacaoResponse resp = ofertaService.criar(usuario.id(), req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resp.id()).toUri();
        return ResponseEntity.created(location).body(resp);
    }

    /**
     * GET /ofertas/minhas — minhas ofertas (DOADOR)
     */
    @GetMapping("/minhas")
    public ResponseEntity<List<OfertaDoacaoResponse>> listarMinhas(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        return ResponseEntity.ok(ofertaService.listarMinhas(usuario.id()));
    }

    /**
     * GET /ofertas — consulta pública paginada (qualquer autenticado)
     * Filtros opcionais: categoria, cidade, estado
     */
    @GetMapping
    public ResponseEntity<Page<OfertaDoacaoResponse>> listarDisponiveis(
            @PageableDefault(size = 20, sort = "criadoEm") Pageable pageable,
            @RequestParam(required = false) TipoItemDoacao categoria,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado) {

        return ResponseEntity.ok(ofertaService.listarDisponiveis(pageable, categoria, cidade, estado));
    }

    /**
     * GET /ofertas/{id} — detalhe da oferta (qualquer autenticado)
     */
    @GetMapping("/{id}")
    public ResponseEntity<OfertaDoacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ofertaService.buscarPorId(id));
    }

    /**
     * PATCH /ofertas/{id}/cancelar — cancela oferta (DOADOR dono)
     * Retorna 204 No Content
     */
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        ofertaService.cancelar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
