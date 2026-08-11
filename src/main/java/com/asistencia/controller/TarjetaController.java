package com.asistencia.controller;

import com.asistencia.entity.Empleado;
import com.asistencia.entity.Tarjeta;
import com.asistencia.repository.EmpleadoRepository;
import com.asistencia.repository.TarjetaRepository;
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

    // Asignar tarjeta a empleado
    @PostMapping("/assign")
    public ResponseEntity<Tarjeta> asignarTarjeta(
            @RequestBody AsignarTarjetaRequest request) {

        if (tarjetaRepository.findByUid(request.getUid()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
                .orElse(null);

        if (empleado == null) {
            return ResponseEntity.notFound().build();
        }

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setUid(request.getUid());
        tarjeta.setEmpleado(empleado);
        tarjeta.setActiva(true);

        return ResponseEntity.ok(tarjetaRepository.save(tarjeta));
    }

    // Listar todas las tarjetas
    @GetMapping
    public List<Tarjeta> listarTarjetas() {
        return tarjetaRepository.findAll();
    }

    // Buscar tarjeta por UID
    @GetMapping("/{uid}")
    public ResponseEntity<Tarjeta> buscarTarjeta(@PathVariable String uid) {

        return tarjetaRepository.findByUid(uid)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Desactivar tarjeta
    @PutMapping("/{uid}/deactivate")
    public ResponseEntity<Tarjeta> desactivarTarjeta(
            @PathVariable String uid) {

        return tarjetaRepository.findByUid(uid)
                .map(tarjeta -> {
                    tarjeta.setActiva(false);
                    return ResponseEntity.ok(tarjetaRepository.save(tarjeta));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Activar tarjeta
    @PutMapping("/{uid}/activate")
    public ResponseEntity<Tarjeta> activarTarjeta(
            @PathVariable String uid) {

        return tarjetaRepository.findByUid(uid)
                .map(tarjeta -> {
                    tarjeta.setActiva(true);
                    return ResponseEntity.ok(tarjetaRepository.save(tarjeta));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    public static class AsignarTarjetaRequest {

        private String uid;
        private Long empleadoId;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public Long getEmpleadoId() {
            return empleadoId;
        }

        public void setEmpleadoId(Long empleadoId) {
            this.empleadoId = empleadoId;
        }
    }
}