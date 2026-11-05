package com.doalink_api.dtos.request;

import com.doalink_api.entities.enums.TipoPessoa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

public record CadastroComumRequest(
        @CPF String cpf,
        @CNPJ String cnpj,
        @NotBlank @Email String email,
        @NotBlank String nome,
        @NotBlank @Size(min = 8, max = 60) String senha
) {

    private boolean temCpf()  { return cpf != null && !cpf.isBlank(); }
    private boolean temCnpj() { return cnpj != null && !cnpj.isBlank(); }

    @JsonIgnore
    @AssertTrue(message = "Informe seu CPF ou CNPJ, apenas um dos dois")
    public boolean isCpfOuCnpjValido() {

        return temCpf() ^ temCnpj();
    }

    @JsonIgnore
    public String getDocumentoPreenchido() {
        return temCpf() ? cpf : cnpj;
    }

    @JsonIgnore
    public TipoPessoa getTipoPessoa() {
        return temCpf() ? TipoPessoa.FISICA : TipoPessoa.JURIDICA;
    }

}
