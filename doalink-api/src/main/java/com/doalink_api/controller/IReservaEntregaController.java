package com.doalink_api.controller;

import com.doalink_api.dtos.request.ConfirmarCodigoTransporteRequest;
import com.doalink_api.dtos.request.ReservaEntregaRequest;
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

@Tag(name = "Reservas e Transportes", description = "Endpoints para reserva de ofertas por receptores e fluxo seguro de transporte por códigos")
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

    @Operation(summary = "Listar transportes disponíveis", description = "Lista reservas aguardando transporte com endereços resumidos (cidade/UF) e sem contatos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Perfil TRANSPORTADOR obrigatório")
    })
    @GetMapping("/aguardando-transporte")
    ResponseEntity<List<ReservaEntregaResponse>> listarAguardandoTransporte();

    @Operation(summary = "Listar minhas reservas (Receptor)", description = "Lista as reservas realizadas pelo RECEPTOR autenticado, incluindo o código de entrega quando em transporte.")
    @GetMapping("/minhas/receptor")
    ResponseEntity<List<ReservaEntregaResponse>> listarMinhasReceptor(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Listar meus transportes (Transportador)", description = "Lista os transportes assumidos pelo TRANSPORTADOR autenticado.")
    @GetMapping("/minhas/transportador")
    ResponseEntity<List<ReservaEntregaResponse>> listarMinhasTransportador(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Listar histórico de transportes concluídos (Transportador)", description = "Lista os transportes concluídos pelo TRANSPORTADOR autenticado em modo somente leitura.")
    @GetMapping("/minhas/transportador/historico")
    ResponseEntity<List<ReservaEntregaResponse>> listarHistoricoTransportador(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Listar reservas das minhas ofertas (Doador)", description = "Lista as reservas associadas às ofertas do DOADOR autenticado.")
    @GetMapping("/minhas/doador")
    ResponseEntity<List<ReservaEntregaResponse>> listarMinhasDoador(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Buscar detalhes da reserva/transporte por ID", description = "Retorna os detalhes da operação conforme a participação do usuário autenticado.")
    @GetMapping("/{id}")
    ResponseEntity<ReservaEntregaResponse> buscarPorId(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Aceitar transporte", description = "Permite a um TRANSPORTADOR ativo assumir uma reserva AGUARDANDO_TRANSPORTE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transporte aceito com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Perfil TRANSPORTADOR obrigatório ou conta suspensa"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflito de situação ou aceite simultâneo")
    })
    @PatchMapping("/{id}/aceitar-transporte")
    ResponseEntity<ReservaEntregaResponse> aceitarTransporte(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Autorizar início do transporte (Doador)", description = "Autoriza o início do transporte e gera o código de confirmação de retirada para o doador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Início autorizado e código de retirada gerado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Apenas o doador dono da oferta pode autorizar"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada"),
            @ApiResponse(responseCode = "409", description = "Operação não está aguardando autorização do doador")
    })
    @PatchMapping("/{id}/autorizar-transporte")
    ResponseEntity<ReservaEntregaResponse> autorizarInicioTransporte(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Confirmar retirada dos itens (Transportador)", description = "Valida o código de retirada fornecido pelo doador e gera o código de entrega para o receptor (válido por 24h).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Retirada confirmada"),
            @ApiResponse(responseCode = "400", description = "Código inválido ou bloqueado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Apenas o transportador vinculado pode confirmar"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada"),
            @ApiResponse(responseCode = "409", description = "Situação incompatível")
    })
    @PatchMapping("/{id}/confirmar-retirada")
    ResponseEntity<ReservaEntregaResponse> confirmarRetirada(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @RequestBody @Valid ConfirmarCodigoTransporteRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Confirmar entrega dos itens (Transportador)", description = "Valida o código de entrega fornecido pelo receptor (validade de 24h) e conclui a operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrega confirmada e operação concluída"),
            @ApiResponse(responseCode = "400", description = "Código inválido ou bloqueado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem permissão ou operação bloqueada/conta suspensa por expiração"),
            @ApiResponse(responseCode = "404", description = "Reserva não encontrada"),
            @ApiResponse(responseCode = "409", description = "Situação incompatível (retirada não confirmada)")
    })
    @PatchMapping("/{id}/confirmar-entrega")
    ResponseEntity<ReservaEntregaResponse> confirmarEntrega(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @RequestBody @Valid ConfirmarCodigoTransporteRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Concluir retirada direta", description = "Marca a reserva e a oferta como CONCLUIDA para retiradas diretas sem transportador.")
    @PatchMapping("/{id}/concluir")
    ResponseEntity<Void> concluir(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);

    @Operation(summary = "Cancelar reserva", description = "Cancela a reserva antes da aceitação do transporte e retorna a oferta para DISPONIVEL.")
    @PatchMapping("/{id}/cancelar")
    ResponseEntity<Void> cancelar(
            @Parameter(description = "ID da reserva") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario);
}
