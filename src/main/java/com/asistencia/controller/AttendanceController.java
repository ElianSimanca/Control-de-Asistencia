package com.asistencia.controller;

import com.asistencia.dto.AttendanceRequest;
import com.asistencia.entity.Empleado;
import com.asistencia.entity.Marcacion;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.entity.Tarjeta;
import com.asistencia.repository.MarcacionRepository;
import com.asistencia.repository.TarjetaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/marcaciones")
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

        // 1. Validar que la tarjeta existe (devuelve 404 si no se encuentra)
        Tarjeta tarjeta = tarjetaRepository.findByUid(request.getUid())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Tarjeta con UID " + request.getUid() + " no registrada"));

        if (!tarjeta.isActiva()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tarjeta inactiva");
        }

        Empleado empleado = tarjeta.getEmpleado();

        if (empleado == null || !empleado.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Empleado inactivo o no asignado a la tarjeta");
        }

        // 2. Determinar si es Entrada o Salida
        TipoMarcacion tipo = TipoMarcacion.ENTRADA;
        var ultimaMarcacion = marcacionRepository.findTopByEmpleadoOrderByFechaHoraDesc(empleado);

        if (ultimaMarcacion.isPresent() && ultimaMarcacion.get().getTipo() == TipoMarcacion.ENTRADA) {
            tipo = TipoMarcacion.SALIDA;
        }

        Marcacion marcacion = new Marcacion();
        marcacion.setEmpleado(empleado);
        marcacion.setTipo(tipo);
        marcacion.setFechaHora(LocalDateTime.now());

        // 3. Lógica de tardanzas protegida contra Nulos
        if (tipo == TipoMarcacion.ENTRADA && empleado.getHorario() != null) {
            LocalTime horaEntrada = empleado.getHorario().getHoraEntrada();
            Integer tolerancia = empleado.getHorario().getToleranciaMinutos();

            if (horaEntrada != null) {
                int minTolerancia = (tolerancia != null) ? tolerancia : 0;
                LocalTime horaLimite = horaEntrada.plusMinutes(minTolerancia);
                LocalTime horaPase = marcacion.getFechaHora().toLocalTime();

                if (horaPase.isAfter(horaLimite)) {
                    Duration retraso = Duration.between(horaEntrada, horaPase);
                    marcacion.setMinutosRetraso((int) retraso.toMinutes());
                }
            }
        }

        return marcacionRepository.save(marcacion);
    }
}