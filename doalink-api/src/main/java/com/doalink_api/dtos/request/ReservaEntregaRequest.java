package com.doalink_api.dtos.request;

import jakarta.validation.constraints.NotNull;

public record ReservaEntregaRequest(
        @NotNull Long ofertaId,
        Boolean precisaTransporte
) {
}

