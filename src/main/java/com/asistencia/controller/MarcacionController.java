package com.asistencia.controller;

import com.asistencia.entity.Empleado;
import com.asistencia.entity.Marcacion;
import com.asistencia.entity.Tarjeta;
import com.asistencia.entity.TipoMarcacion;
import com.asistencia.repository.EmpleadoRepository;
import com.asistencia.repository.MarcacionRepository;
import com.asistencia.repository.TarjetaRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/marcaciones")
public class MarcacionController {

    private final MarcacionRepository marcacionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final TarjetaRepository tarjetaRepository;

    // Tiempo mínimo entre dos lecturas consecutivas
    @Value("${marcacion.anti-duplicado-segundos:5}")
    private long segundosAntiDuplicado;

    public MarcacionController(
            MarcacionRepository marcacionRepository,
            EmpleadoRepository empleadoRepository,
            TarjetaRepository tarjetaRepository) {

        this.marcacionRepository = marcacionRepository;
        this.empleadoRepository = empleadoRepository;
        this.tarjetaRepository = tarjetaRepository;
    }

    // =========================================================
    // TODAS LAS MARCACIONES
    // =========================================================
    @GetMapping
    public List<Marcacion> listarMarcaciones() {
        return marcacionRepository.findAllByOrderByFechaHoraDesc();
    }

    // =========================================================
    // MARCACIONES DE UN EMPLEADO
    // =========================================================
    @GetMapping("/empleado/{id}")
    public ResponseEntity<List<Marcacion>> marcacionesEmpleado(@PathVariable Long id) {
        return empleadoRepository.findById(id)
                .map(empleado -> ResponseEntity.ok(
                        marcacionRepository.findByEmpleadoOrderByFechaHoraDesc(empleado)
                ))
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // MARCACIONES DE UNA FECHA
    // =========================================================
    @GetMapping("/fecha/{fecha}")
    public List<Marcacion> marcacionesFecha(@PathVariable String fecha) {
        LocalDate date = LocalDate.parse(fecha);
        LocalDateTime inicio = date.atStartOfDay();
        LocalDateTime fin = date.atTime(LocalTime.MAX);

        return marcacionRepository.findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin);
    }

    // =========================================================
    // MARCACIÓN RFID (CON CÁLCULO DE RETRASO CORREGIDO)
    // =========================================================
    @PostMapping("/rfid")
    @Transactional
    public ResponseEntity registrarPorRfid(@RequestBody RfidRequest request) {

        // 1. Validar UID
        if (request == null || request.getUid() == null || request.getUid().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new RfidResponse(false, "UID inválido", null));
        }

        String uid = request.getUid().trim().toUpperCase();

        // 2. Buscar tarjeta o REGISTRARLA SI ES NUEVA
        Tarjeta tarjeta = tarjetaRepository.findByUid(uid).orElse(null);
        
        if (tarjeta == null) {
            Tarjeta nuevaTarjeta = new Tarjeta();
            nuevaTarjeta.setUid(uid);
            nuevaTarjeta.setActiva(true);
            nuevaTarjeta.setEmpleado(null); 
            
            tarjetaRepository.save(nuevaTarjeta);

            return ResponseEntity.ok(
                    new RfidResponse(true, "Tarjeta nueva registrada", "REGISTRO")
            );
        }

        // 3. Verificar tarjeta activa
        if (!tarjeta.isActiva()) {
            return ResponseEntity.badRequest()
                    .body(new RfidResponse(false, "La tarjeta está desactivada", null));
        }

        // 4. Obtener empleado
        Empleado empleado = tarjeta.getEmpleado();
        if (empleado == null) {
            return ResponseEntity.badRequest()
                    .body(new RfidResponse(false, "Tarjeta sin empleado asignado", null));
        }

        // 5. HORA ACTUAL CON ZONA HORARIA ESTRICTA
        ZoneId zonaColombia = ZoneId.of("America/Bogota");
        LocalDateTime ahora = LocalDateTime.now(zonaColombia);

        // 6. Antiduplicado
        var ultimaMarcacion = marcacionRepository.findTopByEmpleadoOrderByFechaHoraDesc(empleado);

        if (ultimaMarcacion.isPresent()) {
            LocalDateTime ultimaHora = ultimaMarcacion.get().getFechaHora();
            long segundos = java.time.Duration.between(ultimaHora, ahora).getSeconds();

            if (segundos < segundosAntiDuplicado) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new RfidResponse(false, "Espere " + (segundosAntiDuplicado - segundos) + "s", null));
            }
        }

        // 7. ENTRADA O SALIDA (Evaluando solo el día actual local)
        LocalDate hoy = LocalDate.now(zonaColombia);
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(LocalTime.MAX);

        var ultimaMarcacionHoy = marcacionRepository
                .findTopByEmpleadoAndFechaHoraBetweenOrderByFechaHoraDesc(empleado, inicioDia, finDia);

        TipoMarcacion tipo;

        if (ultimaMarcacionHoy.isEmpty()) {
            tipo = TipoMarcacion.ENTRADA;
        } else {
            TipoMarcacion ultimoTipo = ultimaMarcacionHoy.get().getTipo();
            tipo = (ultimoTipo == TipoMarcacion.ENTRADA) ? TipoMarcacion.SALIDA : TipoMarcacion.ENTRADA;
        }

        // 8. CALCULAR RETRASO (EL ARREGLO ESTÁ AQUÍ)
        int retrasoCalculado = 0;
        if (tipo == TipoMarcacion.ENTRADA) {
            LocalTime horaOficial = LocalTime.of(8, 0); // ⚠️ CAMBIA AQUÍ LA HORA SI ES DIFERENTE
            LocalTime horaLimite = horaOficial.plusMinutes(10); // ⚠️ CAMBIA LOS 10 MINUTOS SI QUIERES
            LocalTime horaReal = ahora.toLocalTime();
            
            if (horaReal.isAfter(horaLimite)) {
                retrasoCalculado = (int) ChronoUnit.MINUTES.between(horaOficial, horaReal);
            }
        }

        // 9. Crear marcación
        Marcacion marcacion = new Marcacion();
        marcacion.setEmpleado(empleado);
        marcacion.setFechaHora(ahora);
        marcacion.setTipo(tipo);
        marcacion.setMinutosRetraso(retrasoCalculado); // <-- SE GUARDA EL RETRASO REAL

        Marcacion guardada = marcacionRepository.save(marcacion);

        // 10. Respuesta exitosa
        return ResponseEntity.ok(
                new RfidResponse(true, "Marcación exitosa", guardada.getTipo().name())
        );
    }

    // =========================================================
    // REQUEST Y RESPONSE RFID
    // =========================================================
    public static class RfidRequest {
        private String uid;

        public String getUid() { return uid; }
        public void setUid(String uid) { this.uid = uid; }
    }

    public static class RfidResponse {
        private boolean success;
        private String mensaje;
        private String tipoMarcacion;

        public RfidResponse(boolean success, String mensaje, String tipoMarcacion) {
            this.success = success;
            this.mensaje = mensaje;
            this.tipoMarcacion = tipoMarcacion;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMensaje() { return mensaje; }
        public void setMensaje(String mensaje) { this.mensaje = mensaje; }
        public String getTipoMarcacion() { return tipoMarcacion; }
        public void setTipoMarcacion(String tipoMarcacion) { this.tipoMarcacion = tipoMarcacion; }
    }
        @DeleteMapping("/limpiar")
    public ResponseEntity<?> limpiarMarcaciones() {
        marcacionRepository.deleteAll();
        return ResponseEntity.ok().build();
    }
}
