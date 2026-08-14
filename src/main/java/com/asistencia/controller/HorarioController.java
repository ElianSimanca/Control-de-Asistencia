package com.asistencia.controller;

import com.asistencia.entity.Horario;
import com.asistencia.repository.HorarioRepository;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        if (!horarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        horarioRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}