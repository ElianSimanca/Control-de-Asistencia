package com.asistencia.repository;

import com.asistencia.entity.Empleado;
import com.asistencia.entity.Marcacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MarcacionRepository extends JpaRepository<Marcacion, Long> {

    Optional<Marcacion> findTopByEmpleadoOrderByFechaHoraDesc(Empleado empleado);

    List<Marcacion> findByEmpleadoOrderByFechaHoraDesc(Empleado empleado);

    List<Marcacion> findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime inicio,
            LocalDateTime fin
    );
}