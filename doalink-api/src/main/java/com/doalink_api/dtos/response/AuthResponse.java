package com.doalink_api.dtos.response;

public record AuthResponse(
        UsuarioResponse usuario,
        String accessToken,
        String refreshToken
) {
}
