package com.doalink_api.dtos.request;

import com.doalink_api.entities.enums.TipoUnidade;
import com.doalink_api.entities.enums.TipoVeiculo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public record DisponibilidadeTransporteRequest(
        @NotNull TipoVeiculo tipoVeiculo,
        @NotNull @Positive Double capacidade,
        @NotNull TipoUnidade tipoUnidade,
        @NotNull @Valid EnderecoRequest origem,
        @NotNull @FutureOrPresent LocalDate dataDisponivel,
        LocalTime horarioDisponivel
) {
}
