package com.doalink_api.repository;

import com.doalink_api.entities.CodigoVerificacaoEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface CodigoVerificacaoEmailRepository extends JpaRepository<CodigoVerificacaoEmail, Long> {

    Optional<CodigoVerificacaoEmail> findFirstByUsuarioIdAndRevogadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(Long usuarioId);

    Optional<CodigoVerificacaoEmail> findFirstByUsuarioEmailAndRevogadoFalseAndUtilizadoFalseOrderByCriadoEmDesc(String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CodigoVerificacaoEmail c SET c.revogado = true WHERE c.usuario.id = :usuarioId AND c.utilizado = false AND c.revogado = false")
    void revogarPendentesDoUsuario(@Param("usuarioId") Long usuarioId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM CodigoVerificacaoEmail c WHERE c.expiracao < :agora")
    void deletarExpirados(@Param("agora") Instant agora);
}
