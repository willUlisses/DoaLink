package com.doalink_api.repository;

import com.doalink_api.entities.Doador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoadorRepository extends JpaRepository<Doador, Long> {

    Optional<Doador> findByEmail(String email);
}
