package com.asistencia.controller;

import com.asistencia.entity.Empleado;
import com.asistencia.repository.EmpleadoRepository;
import com.asistencia.repository.HorarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/empleados")
public class EmpleadoController {

    private final EmpleadoRepository empleadoRepository;
    private final HorarioRepository horarioRepository; // NUEVO

    // Constructor actualizado
    public EmpleadoController(
            EmpleadoRepository empleadoRepository,
            HorarioRepository horarioRepository) {
        this.empleadoRepository = empleadoRepository;
        this.horarioRepository = horarioRepository;
    }

    @PostMapping
    public ResponseEntity crearEmpleado(@RequestBody Empleado empleado) {
        if (empleadoRepository.findByDocumento(empleado.getDocumento()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(empleadoRepository.save(empleado));
    }

    @GetMapping
    public List listarEmpleados() {
        return empleadoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity obtenerEmpleado(@PathVariable Long id) {
        return empleadoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity actualizarEmpleado(
            @PathVariable Long id,
            @RequestBody Empleado datos) {

        return empleadoRepository.findById(id)
                .map(empleado -> {
                    empleado.setNombre(datos.getNombre());
                    empleado.setDocumento(datos.getDocumento());
                    empleado.setCargo(datos.getCargo());
                    empleado.setActivo(datos.isActivo());

                    return ResponseEntity.ok(empleadoRepository.save(empleado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity eliminarEmpleado(@PathVariable Long id) {
        if (!empleadoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        empleadoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // NUEVO ENDPOINT: Asignar Horario
    @PutMapping("/{idEmpleado}/horario/{idHorario}")
    public ResponseEntity asignarHorario(
            @PathVariable Long idEmpleado,
            @PathVariable Long idHorario) {

        var horarioOpt = horarioRepository.findById(idHorario);
        
        if (horarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return empleadoRepository.findById(idEmpleado)
                .map(empleado -> {
                    empleado.setHorario(horarioOpt.get());
                    return ResponseEntity.ok(empleadoRepository.save(empleado));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Empleado> cambiarEstado(@PathVariable Long id) {

        return empleadoRepository.findById(id)
                .map(empleado -> {
                    empleado.setActivo(!empleado.isActivo());

                    return ResponseEntity.ok(
                            empleadoRepository.save(empleado)
                    );
                })
                .orElse(ResponseEntity.notFound().build());
    }
}