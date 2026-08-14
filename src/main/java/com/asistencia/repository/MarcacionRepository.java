package com.asistencia.repository;

import com.asistencia.entity.Empleado;
import com.asistencia.entity.Marcacion;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MarcacionRepository extends JpaRepository<Marcacion, Long> {

    Optional<Marcacion> findTopByEmpleadoOrderByFechaHoraDesc(Empleado empleado);

    List<Marcacion> findByEmpleadoOrderByFechaHoraDesc(Empleado empleado);

    List<Marcacion> findAllByOrderByFechaHoraDesc();

    List<Marcacion> findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    Optional<Marcacion> findTopByEmpleadoAndFechaHoraBetweenOrderByFechaHoraDesc(
            Empleado empleado,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT m
        FROM Marcacion m
        WHERE m.empleado = :empleado
        ORDER BY m.fechaHora DESC
    """)
    List<Marcacion> findUltimasMarcacionesBloqueadas(
            @Param("empleado") Empleado empleado
    );
}