package com.doalink_api.controller;

import com.doalink_api.dtos.request.ContatoRequest;
import com.doalink_api.dtos.request.UsuarioAtualizarRequest;
import com.doalink_api.dtos.response.ContatoResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.dtos.response.UsuarioDetalheResponse;
import com.doalink_api.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UsuarioController implements IUsuarioController {

    private final UsuarioService usuarioService;

    @Override
    public ResponseEntity<UsuarioDetalheResponse> obterMeuPerfil(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(usuarioService.buscarPorId(usuario.id()));
    }

    @Override
    public ResponseEntity<UsuarioDetalheResponse> atualizarMeuPerfil(
            @RequestBody @Valid UsuarioAtualizarRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(usuarioService.atualizar(usuario.id(), req));
    }

    @Override
    public ResponseEntity<ContatoResponse> obterMeuContato(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(usuarioService.obterContato(usuario.id()));
    }

    @Override
    public ResponseEntity<ContatoResponse> atualizarMeuContato(
            @RequestBody @Valid ContatoRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(usuarioService.atualizarContato(usuario.id(), req));
    }

    @Override
    public ResponseEntity<ContatoResponse> atualizarMeuContatoParcial(
            @RequestBody @Valid ContatoRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(usuarioService.atualizarContato(usuario.id(), req));
    }
}
