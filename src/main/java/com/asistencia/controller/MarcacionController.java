package com.asistencia.controller;

import com.asistencia.entity.Empleado;
import com.asistencia.entity.Marcacion;
import com.asistencia.repository.EmpleadoRepository;
import com.asistencia.repository.MarcacionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class MarcacionController {

    private final MarcacionRepository marcacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private Integer minutosRetraso = 0;

    public MarcacionController(
            MarcacionRepository marcacionRepository,
            EmpleadoRepository empleadoRepository) {
        this.marcacionRepository = marcacionRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public Integer getMinutosRetraso() {
        return minutosRetraso;
    }

    public void setMinutosRetraso(Integer minutosRetraso) {
        this.minutosRetraso = minutosRetraso;
    }

    // Todas las marcaciones
    @GetMapping
    public List<Marcacion> listarMarcaciones() {
        return marcacionRepository.findAll();
    }

    // Marcaciones de un empleado
    @GetMapping("/employee/{id}")
    public ResponseEntity<List<Marcacion>> marcacionesEmpleado(
            @PathVariable Long id) {

        return empleadoRepository.findById(id)
                .map(empleado ->
                        ResponseEntity.ok(
                                marcacionRepository
                                        .findByEmpleadoOrderByFechaHoraDesc(empleado)
                        )
                )
                .orElse(ResponseEntity.notFound().build());
    }

    // Marcaciones de una fecha
    @GetMapping("/date/{fecha}")
    public List<Marcacion> marcacionesFecha(
            @PathVariable String fecha) {

        LocalDate date = LocalDate.parse(fecha);

        LocalDateTime inicio = date.atStartOfDay();
        LocalDateTime fin = date.atTime(LocalTime.MAX);

        return marcacionRepository
                .findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin);
    }
}