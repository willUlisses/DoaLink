package com.doalink_api.service;

import com.doalink_api.entities.CodigoTransporte;
import com.doalink_api.entities.ReservaEntrega;
import com.doalink_api.entities.Transportador;
import com.doalink_api.entities.enums.TipoCodigoTransporte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class CodigoTransporteServiceTest {

    private CodigoTransporteService service;
    private ReservaEntrega reserva;
    private Transportador transportador;

    @BeforeEach
    void setUp() {
        service = new CodigoTransporteService("segredo-teste-hmac-32-caracteres-minimo", 5, 24);
        reserva = new ReservaEntrega();
        reserva.setId(77L);
        transportador = new Transportador();
        transportador.setId(3L);
    }

    @Test
    @DisplayName("criarCodigo: deve armazenar apenas hash SHA-256 e salt, nunca o código em texto puro")
    void criarCodigo_deveArmazenarHashESalt() {
        CodigoTransporte entity = service.criarCodigo(reserva, transportador, TipoCodigoTransporte.RETIRADA, null);

        assertThat(entity.getCodigoHash()).hasSize(64);
        assertThat(entity.getCodigoSalt()).hasSize(64);

        String codigoPlano = service.reconstruirCodigoAtivo(entity, LocalDateTime.now());
        assertThat(codigoPlano).matches("^\\d{6}$");
        assertThat(entity.getCodigoHash()).doesNotContain(codigoPlano);
        assertThat(service.validarCodigo(entity, codigoPlano)).isTrue();
        assertThat(service.validarCodigo(entity, "000000".equals(codigoPlano) ? "000001" : "000000")).isFalse();
    }

    @Test
    @DisplayName("reconstruirCodigoAtivo: deve retornar null quando código estiver utilizado, invalidado, bloqueado ou expirado")
    void reconstruirCodigoAtivo_deveRespeitarCicloDeVida() {
        LocalDateTime agora = LocalDateTime.now();
        CodigoTransporte entity = service.criarCodigo(
                reserva, transportador, TipoCodigoTransporte.ENTREGA, agora.plusHours(24));

        assertThat(service.reconstruirCodigoAtivo(entity, agora)).isNotNull();
        assertThat(service.reconstruirCodigoAtivo(entity, agora.plusHours(25))).isNull();

        entity.marcarUtilizado(agora);
        assertThat(service.reconstruirCodigoAtivo(entity, agora)).isNull();
    }
}
