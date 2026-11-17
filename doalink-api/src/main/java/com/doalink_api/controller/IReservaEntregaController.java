package com.doalink_api.controller;

import com.doalink_api.dtos.request.ReservaEntregaRequest;
import com.doalink_api.dtos.request.VincularTransporteRequest;
import com.doalink_api.dtos.response.ReservaEntregaResponse;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reservas e Entregas", description = "Endpoints para reserva de ofertas por receptores e vínculo de transporte por transportadores")
@RequestMapping("/reservas")
public interface IReservaEntregaController {

    @Operation(summary = "Reservar oferta de doação", description = "Permite a um RECEPTOR reservar uma oferta DISPONIVEL, informando se precisa de transporte.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Perfil RECEPTOR obrigatório"),
            @ApiResponse(responseCode = "404", description = "Oferta não encontrada"),
            @ApiResponse(responseCode = "409", description = "Oferta não está DISPONIVEL")
    })
    @PostMapping
    ResponseEntity<ReservaEntregaResponse> reservar(
            @RequestBody @Valid ReservaEntregaRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Listar reservas aguardando transporte", description = "Lista reservas abertas para transporte com endereços resumidos (cidade/UF).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Perfil TRANSPORTADOR obrigatório")
    })
    @GetMapping("/aguardando-transporte")
    ResponseEntity<List<ReservaEntregaResponse>> listarAguardandoTransporte();

    @Operation(summary = "Listar minhas reservas (Receptor)", description = "Lista as reservas realizadas pelo RECEPTOR autenticado.")
    @GetMapping("/minhas/receptor")
    ResponseEntity<List<ReservaEntregaResponse>> listarMinhasReceptor(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Listar minhas entregas (Transportador)", description = "Lista as entregas vinculadas às disponibilidades do TRANSPORTADOR autenticado.")
    @GetMapping("/minhas/transportador")
    ResponseEntity<List<ReservaEntregaResponse>> listarMinhasTransportador(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Listar reservas das minhas ofertas (Doador)", description = "Lista as reservas associadas às ofertas do DOADOR autenticado.")
    @GetMapping("/minhas/doador")
    ResponseEntity<List<ReservaEntregaResponse>> listarMinhasDoador(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Vincular disponibilidade de transporte à reserva", description = "Permite a um TRANSPORTADOR assumir uma reserva AGUARDANDO_TRANSPORTE usando uma disponibilidade compatível.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transporte vinculado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão para a disponibilidade"),
            @ApiResponse(responseCode = "404", description = "Reserva ou disponibilidade não encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflito de situação, unidade ou capacidade insuficiente")
    })
    @PatchMapping("/{id}/vincular-transporte")
    ResponseEntity<ReservaEntregaResponse> vincularTransporte(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @RequestBody @Valid VincularTransporteRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Concluir entrega/retirada", description = "Marca a reserva e a oferta como CONCLUIDA.")
    @PatchMapping("/{id}/concluir")
    ResponseEntity<Void> concluir(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Cancelar reserva", description = "Cancela a reserva antes do vínculo de transporte e retorna a oferta para DISPONIVEL.")
    @PatchMapping("/{id}/cancelar")
    ResponseEntity<Void> cancelar(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);
}

