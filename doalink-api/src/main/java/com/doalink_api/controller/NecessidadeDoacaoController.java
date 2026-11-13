package com.doalink_api.controller;

import com.doalink_api.dtos.request.NecessidadeDoacaoRequest;
import com.doalink_api.dtos.response.NecessidadeDoacaoResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.entities.enums.SituacaoNecessidade;
import com.doalink_api.service.NecessidadeDoacaoService;
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
public class NecessidadeDoacaoController implements INecessidadeDoacaoController {

    private final NecessidadeDoacaoService necessidadeService;

    @Override
    public ResponseEntity<NecessidadeDoacaoResponse> criar(
            @RequestBody @Valid NecessidadeDoacaoRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        NecessidadeDoacaoResponse resp = necessidadeService.criar(usuario.id(), req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resp.id()).toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @Override
    public ResponseEntity<List<NecessidadeDoacaoResponse>> listarMinhas(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(necessidadeService.listarMinhas(usuario.id()));
    }

    @Override
    public ResponseEntity<Page<NecessidadeDoacaoResponse>> listar(
            @RequestParam(required = false) SituacaoNecessidade situacao,
            Pageable pageable) {
        SituacaoNecessidade filtro = situacao != null ? situacao : SituacaoNecessidade.ABERTA;
        return ResponseEntity.ok(necessidadeService.listarPorSituacao(filtro, pageable));
    }

    @Override
    public ResponseEntity<NecessidadeDoacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(necessidadeService.buscarPorId(id));
    }
}
