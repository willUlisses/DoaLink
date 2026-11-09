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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class OfertaDoacaoController implements IOfertaDoacaoController {

    private final OfertaDoacaoService ofertaService;

    @Override
    public ResponseEntity<OfertaDoacaoResponse> criar(
            @RequestBody @Valid OfertaDoacaoRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        OfertaDoacaoResponse resp = ofertaService.criar(usuario.id(), req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resp.id()).toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @Override
    public ResponseEntity<List<OfertaDoacaoResponse>> listarMinhas(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(ofertaService.listarMinhas(usuario.id()));
    }

    @Override
    public ResponseEntity<Page<OfertaDoacaoResponse>> listarDisponiveis(
            Pageable pageable,
            @RequestParam(required = false) TipoItemDoacao categoria,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(ofertaService.listarDisponiveis(pageable, categoria, cidade, estado));
    }

    @Override
    public ResponseEntity<OfertaDoacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ofertaService.buscarPorId(id));
    }

    @Override
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        ofertaService.cancelar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
