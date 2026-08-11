package com.asistencia.controller;

import com.asistencia.dto.AttendanceRequest;
import com.asistencia.entity.Empleado;
import com.asistencia.entity.Marcacion;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.entity.Tarjeta;
import com.asistencia.repository.MarcacionRepository;
import com.asistencia.repository.TarjetaRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final TarjetaRepository tarjetaRepository;
    private final MarcacionRepository marcacionRepository;

    public AttendanceController(
            TarjetaRepository tarjetaRepository,
            MarcacionRepository marcacionRepository) {
        this.tarjetaRepository = tarjetaRepository;
        this.marcacionRepository = marcacionRepository;
    }

    @PostMapping
    public Marcacion registrarAsistencia(@RequestBody AttendanceRequest request) {

        Tarjeta tarjeta = tarjetaRepository.findByUid(request.getUid())
                .orElseThrow(() -> new RuntimeException("Tarjeta no registrada"));

        if (!tarjeta.isActiva()) {
            throw new RuntimeException("Tarjeta inactiva");
        }

        Empleado empleado = tarjeta.getEmpleado();

        if (!empleado.isActivo()) {
            throw new RuntimeException("Empleado inactivo");
        }

        TipoMarcacion tipo = TipoMarcacion.ENTRADA;

        var ultimaMarcacion =
                marcacionRepository.findTopByEmpleadoOrderByFechaHoraDesc(empleado);

        if (ultimaMarcacion.isPresent()
                && ultimaMarcacion.get().getTipo() == TipoMarcacion.ENTRADA) {
            tipo = TipoMarcacion.SALIDA;
        }

        Marcacion marcacion = new Marcacion();
        marcacion.setEmpleado(empleado);
        marcacion.setTipo(tipo);
        marcacion.setFechaHora(LocalDateTime.now());

        return marcacionRepository.save(marcacion);
    }
}