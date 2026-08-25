package com.asistencia.controller;

import com.asistencia.entity.Empleado;
import com.asistencia.entity.Tarjeta;
import com.asistencia.repository.EmpleadoRepository;
import com.asistencia.repository.TarjetaRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cards")
public class TarjetaController {

    private final TarjetaRepository tarjetaRepository;
    private final EmpleadoRepository empleadoRepository;

    public TarjetaController(
            TarjetaRepository tarjetaRepository,
            EmpleadoRepository empleadoRepository) {

        this.tarjetaRepository = tarjetaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    // =========================================================
    // DETECTAR TARJETA
    // =========================================================
    // Este endpoint es utilizado por el ESP32.
    //
    // Si la tarjeta no existe:
    //     -> se registra automáticamente
    //
    // Si ya existe:
    //     -> devuelve la tarjeta existente
    //
    // El usuario nunca introduce ni modifica el UID.
    // =========================================================
    @PostMapping("/detect")
    public ResponseEntity<TarjetaDetectadaResponse> detectarTarjeta(
            @RequestBody DetectarTarjetaRequest request) {

        if (request.getUid() == null ||
                request.getUid().trim().isEmpty()) {

            return ResponseEntity.badRequest().build();
        }

        String uid = request.getUid()
                .trim()
                .toUpperCase();

        // Tarjeta ya existente
        var existente = tarjetaRepository.findByUid(uid);

        if (existente.isPresent()) {

            Tarjeta tarjeta = existente.get();

            return ResponseEntity.ok(
                    new TarjetaDetectadaResponse(
                            tarjeta,
                            false
                    )
            );
        }

        // Tarjeta nueva
        Tarjeta tarjeta = new Tarjeta();

        tarjeta.setUid(uid);
        tarjeta.setActiva(true);
        tarjeta.setEmpleado(null);

        tarjeta = tarjetaRepository.save(tarjeta);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new TarjetaDetectadaResponse(
                                tarjeta,
                                true
                        )
                );
    }

    // =========================================================
    // LISTAR TARJETAS
    // =========================================================
    @GetMapping
    public List<Tarjeta> listarTarjetas() {
        return tarjetaRepository.findAll();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================
    @GetMapping("/id/{id}")
    public ResponseEntity<Tarjeta> buscarPorId(
            @PathVariable Long id) {

        return tarjetaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // BUSCAR POR UID
    // =========================================================
    // Útil internamente para el sistema RFID.
    // =========================================================
    @GetMapping("/uid/{uid}")
    public ResponseEntity<Tarjeta> buscarPorUid(
            @PathVariable String uid) {

        return tarjetaRepository.findByUid(uid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // ASIGNAR TARJETA A EMPLEADO
    // =========================================================
    // El frontend solamente manda:
    //
    // tarjeta ID + empleado ID
    //
    // Nunca manda un UID editable.
    // =========================================================
    @PutMapping("/{id}/assign")
    public ResponseEntity<?> asignarTarjeta(
            @PathVariable Long id,
            @RequestBody AsignarEmpleadoRequest request) {

        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElse(null);

        if (tarjeta == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getEmpleadoId() == null) {
            return ResponseEntity.badRequest().build();
        }

        Empleado empleado = empleadoRepository
                .findById(request.getEmpleadoId())
                .orElse(null);

        if (empleado == null) {
            return ResponseEntity.notFound().build();
        }

        // Verificar que el empleado no tenga otra tarjeta
        var tarjetaExistente =
                tarjetaRepository.findByEmpleadoId(empleado.getId());

        if (tarjetaExistente.isPresent()
                && !tarjetaExistente.get().getId().equals(id)) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("El empleado ya tiene una tarjeta asignada.");
        }

        tarjeta.setEmpleado(empleado);

        return ResponseEntity.ok(
                tarjetaRepository.save(tarjeta)
        );
    }

    // =========================================================
    // DESASIGNAR TARJETA
    // =========================================================
    @PutMapping("/{id}/unassign")
    public ResponseEntity<Tarjeta> desasignarTarjeta(
            @PathVariable Long id) {

        return tarjetaRepository.findById(id)
                .map(tarjeta -> {

                    tarjeta.setEmpleado(null);

                    return ResponseEntity.ok(
                            tarjetaRepository.save(tarjeta)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // ACTIVAR
    // =========================================================
    @PutMapping("/{id}/activate")
    public ResponseEntity<Tarjeta> activarTarjeta(
            @PathVariable Long id) {

        return tarjetaRepository.findById(id)
                .map(tarjeta -> {

                    tarjeta.setActiva(true);

                    return ResponseEntity.ok(
                            tarjetaRepository.save(tarjeta)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // DESACTIVAR
    // =========================================================
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Tarjeta> desactivarTarjeta(
            @PathVariable Long id) {

        return tarjetaRepository.findById(id)
                .map(tarjeta -> {

                    tarjeta.setActiva(false);

                    return ResponseEntity.ok(
                            tarjetaRepository.save(tarjeta)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // ELIMINAR
    // =========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTarjeta(
            @PathVariable Long id) {

        return tarjetaRepository.findById(id)
                .map(tarjeta -> {

                    tarjetaRepository.delete(tarjeta);

                    return ResponseEntity
                            .noContent()
                            .<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // =========================================================
    // REQUESTS
    // =========================================================

    public static class DetectarTarjetaRequest {

        private String uid;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }

    public static class AsignarEmpleadoRequest {

        private Long empleadoId;

        public Long getEmpleadoId() {
            return empleadoId;
        }

        public void setEmpleadoId(Long empleadoId) {
            this.empleadoId = empleadoId;
        }
    }

    // =========================================================
    // RESPONSE DETECCIÓN
    // =========================================================

    public static class TarjetaDetectadaResponse {

        private Long id;
        private String uid;
        private boolean activa;
        private boolean nueva;
        private boolean asignada;

        public TarjetaDetectadaResponse(
                Tarjeta tarjeta,
                boolean nueva) {

            this.id = tarjeta.getId();
            this.uid = tarjeta.getUid();
            this.activa = tarjeta.isActiva();
            this.nueva = nueva;
            this.asignada = tarjeta.getEmpleado() != null;
        }

        public Long getId() {
            return id;
        }

        public String getUid() {
            return uid;
        }

        public boolean isActiva() {
            return activa;
        }

        public boolean isNueva() {
            return nueva;
        }

        public boolean isAsignada() {
            return asignada;
        }
    }
}