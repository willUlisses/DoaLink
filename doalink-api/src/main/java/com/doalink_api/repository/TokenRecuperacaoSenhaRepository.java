package com.doalink_api.repository;

import com.doalink_api.entities.TokenRecuperacaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface TokenRecuperacaoSenhaRepository extends JpaRepository<TokenRecuperacaoSenha, Long> {

    Optional<TokenRecuperacaoSenha> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM TokenRecuperacaoSenha t WHERE t.expiracao < :agora")
    void deletarExpirados(@Param("agora") Instant agora);
}
