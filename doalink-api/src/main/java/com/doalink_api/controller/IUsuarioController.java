package com.doalink_api.controller;

import com.doalink_api.dtos.request.ContatoRequest;
import com.doalink_api.dtos.request.UsuarioAtualizarRequest;
import com.doalink_api.dtos.response.ContatoResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.dtos.response.UsuarioDetalheResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Perfil e Contato", description = "Endpoints de consulta e atualização do perfil autenticado e opções de contato")
@RequestMapping("/usuarios")
public interface IUsuarioController {

    @Operation(summary = "Obter perfil do usuário autenticado")
    @GetMapping("/me")
    ResponseEntity<UsuarioDetalheResponse> obterMeuPerfil(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Atualizar endereço do perfil do usuário autenticado")
    @PutMapping("/me")
    ResponseEntity<UsuarioDetalheResponse> atualizarMeuPerfil(
            @RequestBody @Valid UsuarioAtualizarRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Obter opções de contato do usuário autenticado")
    @GetMapping("/me/contato")
    ResponseEntity<ContatoResponse> obterMeuContato(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Atualizar opções de contato do usuário autenticado")
    @PutMapping("/me/contato")
    ResponseEntity<ContatoResponse> atualizarMeuContato(
            @RequestBody @Valid ContatoRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Atualizar parcialmente opções de contato do usuário autenticado")
    @PatchMapping("/me/contato")
    ResponseEntity<ContatoResponse> atualizarMeuContatoParcial(
            @RequestBody @Valid ContatoRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);
}
