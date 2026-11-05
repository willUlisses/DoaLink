package com.doalink_api.dtos.response;

import lombok.Builder;

@Builder
public record ErrorResponse(
        String message,
        Integer status
) {

    public static ErrorResponse from(RuntimeException e, Integer status) {
        return new ErrorResponse(
                e.getMessage(),
                status
        );
    }

}
