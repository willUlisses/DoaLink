package com.doalink_api.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CampanhaEmergencialRequest(
        @NotBlank(message = "O nome da campanha é obrigatório")
        @Size(max = 150, message = "O nome da campanha deve ter no máximo 150 caracteres")
        String nome,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        String descricao,

        @NotNull(message = "O endereço de recebimento é obrigatório")
        @Valid
        EnderecoRequest enderecoRecebimento,

        @Size(max = 50, message = "A campanha pode ter no máximo 50 itens de necessidade")
        List<@Valid ItemCampanhaRequest> itens
) {
}
