package com.doalink_api.integration;

import com.doalink_api.dtos.request.ConfirmarCodigoTransporteRequest;
import com.doalink_api.dtos.request.ReservaEntregaRequest;
import com.doalink_api.dtos.response.UsuarioAutenticado;
import com.doalink_api.entities.*;
import com.doalink_api.entities.enums.*;
import com.doalink_api.repository.*;
import com.doalink_api.service.CodigoTransporteService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class TransporteFluxoIntegrationTest {

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private OfertaDoacaoRepository ofertaRepository;
    @Autowired
    private ReservaEntregaRepository reservaRepository;
    @Autowired
    private CodigoTransporteRepository codigoTransporteRepository;
    @Autowired
    private EventoTransporteRepository eventoTransporteRepository;
    @Autowired
    private OcorrenciaTransporteRepository ocorrenciaTransporteRepository;
    @Autowired
    private CodigoTransporteService codigoTransporteService;
    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private MockMvc mockMvc;

    private Doador doador;
    private Doador outroDoador;
    private Receptor receptor;
    private Receptor outroReceptor;
    private Transportador transportador1;
    private Transportador transportador2;
    private OfertaDoacao oferta;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        ocorrenciaTransporteRepository.deleteAll();
        eventoTransporteRepository.deleteAll();
        codigoTransporteRepository.deleteAll();
        reservaRepository.deleteAll();
        ofertaRepository.deleteAll();

        String suffix = String.valueOf(System.nanoTime());
        long base = System.nanoTime() % 1_000_000_000L;

        doador = new Doador(String.format("11%09d", base), "Doador Fluxo", "doador.fluxo." + suffix + "@doalink.org", "hash", "11999990001", TipoPessoa.FISICA);
        doador.setEmailVerificado(true);
        doador = usuarioRepository.save(doador);

        outroDoador = new Doador(String.format("22%09d", base), "Outro Doador", "outro.doador." + suffix + "@doalink.org", "hash", "11999990011", TipoPessoa.FISICA);
        outroDoador.setEmailVerificado(true);
        outroDoador = usuarioRepository.save(outroDoador);

        receptor = new Receptor();
        receptor.setDocumento(String.format("33000%09d", base));
        receptor.setNome("ONG Fluxo");
        receptor.setEmail("ong.fluxo." + suffix + "@doalink.org");
        receptor.setSenha("hash");
        receptor.setTipoPessoa(TipoPessoa.JURIDICA);
        receptor.setEmailVerificado(true);
        receptor.setEnderecoOrganizacao(enderecoExemplo("Av. Paulista", "1000", "São Paulo", "SP", "01310100"));
        receptor = usuarioRepository.save(receptor);

        outroReceptor = new Receptor();
        outroReceptor.setDocumento(String.format("44000%09d", base));
        outroReceptor.setNome("Outra ONG");
        outroReceptor.setEmail("outra.ong." + suffix + "@doalink.org");
        outroReceptor.setSenha("hash");
        outroReceptor.setTipoPessoa(TipoPessoa.JURIDICA);
        outroReceptor.setEmailVerificado(true);
        outroReceptor.setEnderecoOrganizacao(enderecoExemplo("Rua B", "20", "São Paulo", "SP", "01310200"));
        outroReceptor = usuarioRepository.save(outroReceptor);

        transportador1 = new Transportador(String.format("55%09d", base), "Motorista Um", "mot1." + suffix + "@doalink.org", "hash", "11999990003", TipoPessoa.FISICA);
        transportador1.setEmailVerificado(true);
        transportador1 = usuarioRepository.save(transportador1);

        transportador2 = new Transportador(String.format("66%09d", base), "Motorista Dois", "mot2." + suffix + "@doalink.org", "hash", "11999990004", TipoPessoa.FISICA);
        transportador2.setEmailVerificado(true);
        transportador2 = usuarioRepository.save(transportador2);

        oferta = new OfertaDoacao(doador, enderecoExemplo("Rua das Flores", "50", "Campinas", "SP", "13010000"));
        ItemDoacao item = new ItemDoacao("Arroz tipo 1", TipoItemDoacao.NAO_PERECIVEL, 25.0);
        item.setTipoUnidade(TipoUnidade.KG);
        oferta.adicionarItem(item);
        oferta = ofertaRepository.save(oferta);
    }

    private Endereco enderecoExemplo(String logradouro, String numero, String cidade, String estado, String cep) {
        Endereco e = new Endereco();
        e.setLogradouro(logradouro);
        e.setNumero(numero);
        e.setBairro("Centro");
        e.setCidade(cidade);
        e.setEstado(estado);
        e.setCep(cep);
        return e;
    }

    private RequestPostProcessor asUser(Usuario u) {
        var auth = new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(u.getId(), u.getEmail()),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getTipoUsuario().name())));
        return authentication(auth);
    }

    @Test
    @DisplayName("Fluxo completo integrado: reserva -> aceite -> autorização doador -> retirada -> código 24h -> entrega + segurança")
    void deveExecutarFluxoCompletoComSegurancaEHashNoBanco() throws Exception {
        // 0. Rotas de disponibilidade não existem mais (404/403)
        mockMvc.perform(get("/disponibilidades/minhas").with(asUser(transportador1)))
                .andExpect(status().is4xxClientError());

        // 1. Receptor reserva a oferta solicitando transporte
        MvcResult reservaRes = mockMvc.perform(post("/reservas")
                        .with(asUser(receptor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReservaEntregaRequest(oferta.getId(), true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("AGUARDANDO_TRANSPORTE"))
                .andReturn();
        Long reservaId = objectMapper.readTree(reservaRes.getResponse().getContentAsString()).get("id").asLong();

        // 2. Transportador 1 aceita o transporte; Transportador 2 tentando assumir recebe 409 Conflict
        mockMvc.perform(patch("/reservas/" + reservaId + "/aceitar-transporte")
                        .with(asUser(transportador1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("AGUARDANDO_AUTORIZACAO_DOADOR"))
                .andExpect(jsonPath("$.doador.email").value(doador.getEmail()))
                .andExpect(jsonPath("$.receptor.email").value(receptor.getEmail()));

        mockMvc.perform(patch("/reservas/" + reservaId + "/aceitar-transporte")
                        .with(asUser(transportador2)))
                .andExpect(status().isConflict());

        // 3. Outro doador não consegue autorizar oferta de terceiros (403)
        mockMvc.perform(patch("/ofertas/" + oferta.getId() + "/autorizar-transporte")
                        .with(asUser(outroDoador)))
                .andExpect(status().isForbidden());

        // 4. Doador dono autoriza o início do transporte e obtém código de retirada
        MvcResult authRes = mockMvc.perform(patch("/ofertas/" + oferta.getId() + "/autorizar-transporte")
                        .with(asUser(doador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("AGUARDANDO_CONFIRMACAO_RETIRADA"))
                .andReturn();
        String codigoRetirada = objectMapper.readTree(authRes.getResponse().getContentAsString())
                .get("codigoRetirada").asText();
        assertThat(codigoRetirada).matches("^\\d{6}$");

        // Garantia de que o banco NÃO guarda o código em texto puro
        CodigoTransporte codRetiradaDb = codigoTransporteRepository
                .findFirstByReservaIdAndTipoOrderByCriadoEmDesc(reservaId, TipoCodigoTransporte.RETIRADA)
                .orElseThrow();
        assertThat(codRetiradaDb.getCodigoHash()).isNotEqualTo(codigoRetirada).hasSize(64);

        // Transportador NÃO recebe o código de retirada ao consultar "Meus transportes"
        mockMvc.perform(get("/reservas/minhas/transportador").with(asUser(transportador1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigoRetirada").doesNotExist());

        // 5. Outro motorista tentando confirmar retirada recebe 403
        mockMvc.perform(patch("/reservas/" + reservaId + "/confirmar-retirada")
                        .with(asUser(transportador2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarCodigoTransporteRequest(codigoRetirada))))
                .andExpect(status().isForbidden());

        // 6. Motorista vinculado informa código errado (400 e não muda status), depois informa código correto
        String codigoErrado = "000000".equals(codigoRetirada) ? "000001" : "000000";
        mockMvc.perform(patch("/reservas/" + reservaId + "/confirmar-retirada")
                        .with(asUser(transportador1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarCodigoTransporteRequest(codigoErrado))))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/reservas/" + reservaId + "/confirmar-retirada")
                        .with(asUser(transportador1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarCodigoTransporteRequest(codigoRetirada))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("EM_TRANSPORTE"))
                .andExpect(jsonPath("$.codigoEntrega").doesNotExist());

        // Reutilização do primeiro código rejeitada
        mockMvc.perform(patch("/reservas/" + reservaId + "/confirmar-retirada")
                        .with(asUser(transportador1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarCodigoTransporteRequest(codigoRetirada))))
                .andExpect(status().isConflict());

        // 7. Receptor dono visualiza código de entrega com validade de 24h; outro receptor não acessa (403)
        MvcResult receptorList = mockMvc.perform(get("/reservas/minhas/receptor").with(asUser(receptor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].situacao").value("EM_TRANSPORTE"))
                .andExpect(jsonPath("$[0].codigoEntrega").isNotEmpty())
                .andExpect(jsonPath("$[0].codigoEntregaExpiraEm").isNotEmpty())
                .andReturn();
        JsonNode recNode = objectMapper.readTree(receptorList.getResponse().getContentAsString()).get(0);
        String codigoEntrega = recNode.get("codigoEntrega").asText();
        assertThat(codigoEntrega).matches("^\\d{6}$");

        mockMvc.perform(get("/reservas/" + reservaId).with(asUser(outroReceptor)))
                .andExpect(status().isForbidden());

        // 8. Motorista confirma entrega com código correto -> CONCLUIDA e histórico preenchido
        mockMvc.perform(patch("/reservas/" + reservaId + "/confirmar-entrega")
                        .with(asUser(transportador1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarCodigoTransporteRequest(codigoEntrega))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("CONCLUIDA"))
                .andExpect(jsonPath("$.situacaoOferta").value("CONCLUIDA"));

        mockMvc.perform(get("/reservas/minhas/transportador/historico").with(asUser(transportador1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reservaId))
                .andExpect(jsonPath("$[0].situacao").value("CONCLUIDA"));
    }

    @Test
    @DisplayName("Expiração do código de 24h suspende transportador no banco, bloqueia novo aceite e registra ocorrência")
    void deveSuspenderTransportadorQuandoCodigoEntregaExpirar() throws Exception {
        ReservaEntrega reserva = new ReservaEntrega();
        reserva.setOferta(oferta);
        reserva.setReceptor(receptor);
        reserva.setTransportador(transportador1);
        reserva.setPrecisaTransporte(true);
        reserva.setSituacao(SituacaoReservaEntrega.EM_TRANSPORTE);
        reserva.setAceitoEm(LocalDateTime.now().minusHours(26));
        reserva.setAutorizadoEm(LocalDateTime.now().minusHours(25));
        reserva.setRetiradaConfirmadaEm(LocalDateTime.now().minusHours(25));
        oferta.setSituacao(SituacaoOferta.RESERVADA);
        ofertaRepository.save(oferta);
        reserva = reservaRepository.save(reserva);

        CodigoTransporte codigoExpirado = codigoTransporteService.criarCodigo(
                reserva, transportador1, TipoCodigoTransporte.ENTREGA, LocalDateTime.now().minusMinutes(5));
        codigoTransporteRepository.save(codigoExpirado);
        entityManager.flush();

        // Tentativa de uso do código expirado deve bloquear (403)
        mockMvc.perform(patch("/reservas/" + reserva.getId() + "/confirmar-entrega")
                        .with(asUser(transportador1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarCodigoTransporteRequest("123456"))))
                .andExpect(status().isForbidden());

        entityManager.flush();
        entityManager.clear();

        Transportador transpAtualizado = (Transportador) usuarioRepository.findById(transportador1.getId()).orElseThrow();
        assertThat(transpAtualizado.getSituacao()).isEqualTo(SituacaoTransportador.SUSPENSO);

        ReservaEntrega reservaAtualizada = reservaRepository.findById(reserva.getId()).orElseThrow();
        assertThat(reservaAtualizada.getSituacao()).isEqualTo(SituacaoReservaEntrega.EM_INVESTIGACAO);
        assertThat(reservaAtualizada.getOferta().getSituacao()).isEqualTo(SituacaoOferta.RESERVADA);

        OcorrenciaTransporte ocorrencia = ocorrenciaTransporteRepository.findByReservaId(reserva.getId()).orElseThrow();
        assertThat(ocorrencia.getSituacao()).isEqualTo(SituacaoOcorrenciaTransporte.PENDENTE_INVESTIGACAO);

        // Transportador suspenso não consegue aceitar outro transporte (403)
        OfertaDoacao outraOferta = new OfertaDoacao(doador, enderecoExemplo("Rua X", "10", "Campinas", "SP", "13010000"));
        ItemDoacao item2 = new ItemDoacao("Feijão", TipoItemDoacao.NAO_PERECIVEL, 10.0);
        item2.setTipoUnidade(TipoUnidade.KG);
        outraOferta.adicionarItem(item2);
        outraOferta = ofertaRepository.save(outraOferta);

        ReservaEntrega novaReserva = new ReservaEntrega();
        novaReserva.setOferta(outraOferta);
        novaReserva.setReceptor(receptor);
        novaReserva.setPrecisaTransporte(true);
        novaReserva.setSituacao(SituacaoReservaEntrega.AGUARDANDO_TRANSPORTE);
        novaReserva = reservaRepository.save(novaReserva);

        mockMvc.perform(patch("/reservas/" + novaReserva.getId() + "/aceitar-transporte")
                        .with(asUser(transportador1)))
                .andExpect(status().isForbidden());
    }
}
