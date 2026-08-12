package com.asistencia.repository;

import com.asistencia.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

// Fíjate bien en la parte de <Horario, Long>
public interface HorarioRepository extends JpaRepository<Horario, Long> {
}