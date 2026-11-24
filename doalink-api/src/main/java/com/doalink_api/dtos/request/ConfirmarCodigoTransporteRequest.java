package com.doalink_api.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ConfirmarCodigoTransporteRequest(
        @NotBlank(message = "O código de confirmação é obrigatório")
        @Pattern(regexp = "^\\d{6}$", message = "O código deve conter exatamente 6 dígitos numéricos")
        String codigo
) {
}
