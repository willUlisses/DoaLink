package com.doalink_api.dtos.request;

import jakarta.validation.constraints.NotNull;

public record VincularTransporteRequest(
        @NotNull Long disponibilidadeId
) {
}

