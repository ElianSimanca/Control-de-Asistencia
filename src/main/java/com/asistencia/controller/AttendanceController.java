package com.asistencia.controller;

import com.asistencia.dto.AttendanceRequest;
import com.asistencia.entity.Empleado;
import com.asistencia.entity.Marcacion;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.entity.Tarjeta;
import com.asistencia.repository.EmpleadoRepository;
import com.asistencia.repository.MarcacionRepository;
import com.asistencia.repository.TarjetaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/marcaciones")
public class AttendanceController {

    private final TarjetaRepository tarjetaRepository;
    private final MarcacionRepository marcacionRepository;
    private final EmpleadoRepository empleadoRepository;

    public AttendanceController(
            TarjetaRepository tarjetaRepository,
            MarcacionRepository marcacionRepository,
            EmpleadoRepository empleadoRepository) {
        this.tarjetaRepository = tarjetaRepository;
        this.marcacionRepository = marcacionRepository;
        this.empleadoRepository = empleadoRepository;
    }

    @PostMapping
    public Marcacion registrarAsistencia(@RequestBody AttendanceRequest request) {

        Empleado empleado = null;

        // 1. Buscar empleado (por UID del ESP32 o por ID del React)
        if (request.getUid() != null && !request.getUid().isEmpty()) {
            Tarjeta tarjeta = tarjetaRepository.findByUid(request.getUid())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Tarjeta con UID " + request.getUid() + " no registrada"));

            if (!tarjeta.isActiva()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarjeta inactiva");
            }
            empleado = tarjeta.getEmpleado();
        } else if (request.getEmpleadoId() != null) {
            empleado = empleadoRepository.findById(request.getEmpleadoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Empleado no encontrado con ID: " + request.getEmpleadoId()));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar un UID o un Empleado ID");
        }

        if (empleado == null || !empleado.isActivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empleado inactivo o no asignado");
        }

        // 2. Determinar Fecha/Hora (Usa la de React, o la actual si viene del ESP32)
        LocalDateTime horaMarcacion;
        if (request.getFechaHora() != null) {
            // Convertimos la hora ISO que envía React a la zona de Colombia
            horaMarcacion = request.getFechaHora()
                    .atZoneSameInstant(ZoneId.of("America/Bogota"))
                    .toLocalDateTime();
        } else {
            horaMarcacion = LocalDateTime.now(ZoneId.of("America/Bogota"));
        }

        // 3. Determinar Tipo (Usa el de React, o el automático del ESP32)
        TipoMarcacion tipo;
        if (request.getTipo() != null && (request.getTipo().equals("ENTRADA") || request.getTipo().equals("SALIDA"))) {
            tipo = TipoMarcacion.valueOf(request.getTipo());
        } else {
            // Lógica automática (La que ya tenía tu compañero)
            tipo = TipoMarcacion.ENTRADA;
            var ultimaMarcacion = marcacionRepository.findTopByEmpleadoOrderByFechaHoraDesc(empleado);
            if (ultimaMarcacion.isPresent() && ultimaMarcacion.get().getTipo() == TipoMarcacion.ENTRADA) {
                tipo = TipoMarcacion.SALIDA;
            }
        }

        Marcacion marcacion = new Marcacion();
        marcacion.setEmpleado(empleado);
        marcacion.setTipo(tipo);
        marcacion.setFechaHora(horaMarcacion);

        // 4. Lógica de tardanzas (La de tu compañero)
        if (tipo == TipoMarcacion.ENTRADA && empleado.getHorario() != null) {
            LocalTime horaEntrada = empleado.getHorario().getHoraEntrada();
            Integer tolerancia = empleado.getHorario().getToleranciaMinutos();

            if (horaEntrada != null) {
                int minTolerancia = (tolerancia != null) ? tolerancia : 0;
                LocalTime horaLimite = horaEntrada.plusMinutes(minTolerancia);
                LocalTime horaPase = horaMarcacion.toLocalTime();

                if (horaPase.isAfter(horaLimite)) {
                    Duration retraso = Duration.between(horaEntrada, horaPase);
                    marcacion.setMinutosRetraso((int) retraso.toMinutes());
                }
            }
        }

        return marcacionRepository.save(marcacion);
    }
}
