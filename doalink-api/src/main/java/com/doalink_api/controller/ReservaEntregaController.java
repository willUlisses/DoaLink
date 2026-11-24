package com.doalink_api.controller;

import com.doalink_api.dtos.request.ConfirmarCodigoTransporteRequest;
import com.doalink_api.dtos.request.ReservaEntregaRequest;
import com.doalink_api.dtos.response.ReservaEntregaResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.service.ReservaEntregaService;
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
public class ReservaEntregaController implements IReservaEntregaController {

    private final ReservaEntregaService reservaService;

    @Override
    public ResponseEntity<ReservaEntregaResponse> reservar(
            @RequestBody @Valid ReservaEntregaRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        ReservaEntregaResponse resp = reservaService.reservar(usuario.id(), req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(resp.id()).toUri();
        return ResponseEntity.created(location).body(resp);
    }

    @Override
    public ResponseEntity<List<ReservaEntregaResponse>> listarAguardandoTransporte() {
        return ResponseEntity.ok(reservaService.listarAguardandoTransporte());
    }

    @Override
    public ResponseEntity<List<ReservaEntregaResponse>> listarMinhasReceptor(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(reservaService.listarMinhasReceptor(usuario.id()));
    }

    @Override
    public ResponseEntity<List<ReservaEntregaResponse>> listarMinhasTransportador(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(reservaService.listarMinhasTransportador(usuario.id()));
    }

    @Override
    public ResponseEntity<List<ReservaEntregaResponse>> listarHistoricoTransportador(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(reservaService.listarHistoricoTransportador(usuario.id()));
    }

    @Override
    public ResponseEntity<List<ReservaEntregaResponse>> listarMinhasDoador(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(reservaService.listarMinhasDoador(usuario.id()));
    }

    @Override
    public ResponseEntity<ReservaEntregaResponse> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(reservaService.buscarPorId(usuario.id(), id));
    }

    @Override
    public ResponseEntity<ReservaEntregaResponse> aceitarTransporte(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(reservaService.aceitarTransporte(usuario.id(), id));
    }

    @Override
    public ResponseEntity<ReservaEntregaResponse> autorizarInicioTransporte(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(reservaService.autorizarInicioTransporte(usuario.id(), id));
    }

    @Override
    public ResponseEntity<ReservaEntregaResponse> confirmarRetirada(
            @PathVariable Long id,
            @RequestBody @Valid ConfirmarCodigoTransporteRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(reservaService.confirmarRetirada(usuario.id(), id, req));
    }

    @Override
    public ResponseEntity<ReservaEntregaResponse> confirmarEntrega(
            @PathVariable Long id,
            @RequestBody @Valid ConfirmarCodigoTransporteRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(reservaService.confirmarEntrega(usuario.id(), id, req));
    }

    @Override
    public ResponseEntity<Void> concluir(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        reservaService.concluir(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        reservaService.cancelar(usuario.id(), id);
        return ResponseEntity.noContent().build();
    }
}
