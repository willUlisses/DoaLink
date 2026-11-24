package com.doalink_api.entities;

import com.doalink_api.entities.enums.SituacaoTransportador;
import com.doalink_api.entities.enums.TipoPessoa;
import com.doalink_api.entities.enums.TipoUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transportadores")
public class Transportador extends Usuario {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SituacaoTransportador situacao = SituacaoTransportador.ATIVO;

    @Column(name = "suspenso_em")
    private LocalDateTime suspensoEm;

    @Column(name = "motivo_suspensao", length = 500)
    private String motivoSuspensao;

    public Transportador() {
        setTipoUsuario(TipoUsuario.TRANSPORTADOR);
    }

    /** Usado pelo AuthService no cadastro (telefone opcional). */
    public Transportador(String documento, String nome, String email, String senha, TipoPessoa tipoPessoa) {
        super(documento, nome, email, senha, null, TipoUsuario.TRANSPORTADOR, tipoPessoa);
    }

    /** Construtor completo com telefone. */
    public Transportador(String documento, String nome, String email, String senha, String telefone, TipoPessoa tipoPessoa) {
        super(documento, nome, email, senha, telefone, TipoUsuario.TRANSPORTADOR, tipoPessoa);
    }

    public boolean isSuspenso() {
        return this.situacao == SituacaoTransportador.SUSPENSO;
    }
}
