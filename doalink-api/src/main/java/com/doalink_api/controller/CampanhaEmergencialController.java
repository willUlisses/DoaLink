package com.doalink_api.controller;

import com.doalink_api.dtos.request.CampanhaEmergencialRequest;
import com.doalink_api.dtos.response.CampanhaEmergencialResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.entities.enums.SituacaoCampanha;
import com.doalink_api.service.CampanhaEmergencialService;
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
public class CampanhaEmergencialController implements ICampanhaEmergencialController {

    private final CampanhaEmergencialService campanhaService;

    @Override
    public ResponseEntity<CampanhaEmergencialResponse> criar(
            @RequestBody @Valid CampanhaEmergencialRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        CampanhaEmergencialResponse resp = campanhaService.criar(usuario.id(), req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resp.id()).toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @Override
    public ResponseEntity<List<CampanhaEmergencialResponse>> listarMinhas(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(campanhaService.listarMinhas(usuario.id()));
    }

    @Override
    public ResponseEntity<Page<CampanhaEmergencialResponse>> listar(
            @RequestParam(required = false) SituacaoCampanha situacao,
            Pageable pageable) {
        SituacaoCampanha filtro = situacao != null ? situacao : SituacaoCampanha.ATIVA;
        return ResponseEntity.ok(campanhaService.listarPorSituacao(filtro, pageable));
    }

    @Override
    public ResponseEntity<CampanhaEmergencialResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(campanhaService.buscarPorId(id));
    }
}
