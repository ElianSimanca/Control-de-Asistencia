package com.asistencia.controller;

import com.asistencia.entity.Horario;
import com.asistencia.repository.HorarioRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioRepository horarioRepository;

    public HorarioController(HorarioRepository horarioRepository) {
        this.horarioRepository = horarioRepository;
    }

    @GetMapping
    public List<Horario> listar() {
        return horarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Horario> obtener(@PathVariable Long id) {
        return horarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Horario crear(@RequestBody Horario horario) {
        return horarioRepository.save(horario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Horario> actualizar(
            @PathVariable Long id,
            @RequestBody Horario datos) {

        return horarioRepository.findById(id)
                .map(horario -> {
                    horario.setNombre(datos.getNombre());
                    horario.setHoraEntrada(datos.getHoraEntrada());
                    horario.setHoraSalida(datos.getHoraSalida());
                    horario.setToleranciaMinutos(datos.getToleranciaMinutos());

                    return ResponseEntity.ok(
                            horarioRepository.save(horario)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // En tu HorarioController.java
@DeleteMapping("/{id}")
public ResponseEntity<?> eliminar(@PathVariable Long id) {
    try {
        if (!horarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        horarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    } catch (DataIntegrityViolationException e) {
        // Devuelve un error 400 (Bad Request) o 409 (Conflict) si está en uso
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("No se puede eliminar el horario porque está asignado a uno o más empleados.");
    }
}
}