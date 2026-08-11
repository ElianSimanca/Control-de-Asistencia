package com.asistencia.repository;

import com.asistencia.entity.Tarjeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TarjetaRepository extends JpaRepository<Tarjeta, Long> {

    Optional<Tarjeta> findByUid(String uid);

    List<Tarjeta> findByActiva(boolean activa);
}