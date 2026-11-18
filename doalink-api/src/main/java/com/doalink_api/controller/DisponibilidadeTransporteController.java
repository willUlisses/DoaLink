package com.doalink_api.controller;

import com.doalink_api.dtos.request.DisponibilidadeTransporteRequest;
import com.doalink_api.dtos.response.DisponibilidadeTransporteResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.service.DisponibilidadeTransporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class DisponibilidadeTransporteController implements IDisponibilidadeTransporteController {

    private final DisponibilidadeTransporteService disponibilidadeService;

    @Override
    public ResponseEntity<DisponibilidadeTransporteResponse> criar(
            @RequestBody @Valid DisponibilidadeTransporteRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        DisponibilidadeTransporteResponse resp = disponibilidadeService.criar(usuario.id(), req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resp.id()).toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @Override
    public ResponseEntity<List<DisponibilidadeTransporteResponse>> listarMinhas(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(disponibilidadeService.listarMinhas(usuario.id()));
    }

    @Override
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        disponibilidadeService.cancelar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> encerrar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        disponibilidadeService.encerrar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}

