package com.doalink_api.controller;

import com.doalink_api.dtos.request.RedefinirSenhaRequest;
import com.doalink_api.dtos.request.SolicitarRecuperacaoSenhaRequest;
import com.doalink_api.dtos.response.MensagemResponse;
import com.doalink_api.service.RecuperacaoSenhaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
public class RecuperacaoSenhaController implements IRecuperacaoSenhaController {

    private final RecuperacaoSenhaService recuperacaoSenhaService;

    @Override
    public ResponseEntity<MensagemResponse> solicitarRecuperacao(SolicitarRecuperacaoSenhaRequest request) {
        recuperacaoSenhaService.solicitarRecuperacao(request.email());
        return ResponseEntity.ok(new MensagemResponse(
                "Se o e-mail informado estiver cadastrado, você receberá um link para redefinir sua senha em breve."));
    }

    @Override
    public ResponseEntity<Map<String, Object>> validarToken(String token) {
        boolean valido = recuperacaoSenhaService.validarToken(token);
        return ResponseEntity.ok(Map.of("valido", valido));
    }

    @Override
    public ResponseEntity<MensagemResponse> redefinirSenha(RedefinirSenhaRequest request) {
        recuperacaoSenhaService.redefinirSenha(request.token(), request.novaSenha());
        return ResponseEntity.ok(new MensagemResponse("Senha redefinida com sucesso."));
    }
}
