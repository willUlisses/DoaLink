package com.doalink_api.repository;

import com.doalink_api.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken r set r.revogado = true where r.usuario.id = :usuarioId and r.revogado = false")
    void revogarTodosDoUsuario(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("delete from RefreshToken r where r.expiraEm < :agora")
    void deletarExpirados(@Param("agora") Instant agora);

}
