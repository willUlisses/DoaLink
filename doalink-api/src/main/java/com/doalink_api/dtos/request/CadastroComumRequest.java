package com.doalink_api.dtos.request;

import com.doalink_api.entities.enums.TipoUsuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

public record CadastroComumRequest(
        @CPF String cpf,
        @CNPJ String cnpj,
        @NotBlank @Email String email,
        @NotBlank String nome,
        @NotBlank @Size(min = 8, max = 20) String senha
) {

    @JsonIgnore
    @AssertTrue(message = "Informe CPF ou CNPJ, apenas um dos dois")
    public boolean isCpfOuCnpjValido() {
        boolean temCpf = cpf != null && !cpf.isBlank();
        boolean temCnpj = cnpj != null && !cnpj.isBlank();
        return temCpf != temCnpj;
    }

}
