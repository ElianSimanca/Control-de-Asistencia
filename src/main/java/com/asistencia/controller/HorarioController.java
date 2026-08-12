package com.asistencia.controller;

import com.asistencia.entity.Horario;
import com.asistencia.repository.HorarioRepository;
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
    public List listar() {
        return horarioRepository.findAll();
    }

    @PostMapping
    public Horario crear(@RequestBody Horario horario) {
        return horarioRepository.save(horario);
    }
}