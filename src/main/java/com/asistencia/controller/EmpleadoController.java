package com.asistencia.controller;

import com.asistencia.entity.Empleado;
import com.asistencia.repository.EmpleadoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmpleadoController {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoController(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @PostMapping
    public ResponseEntity<Empleado> crearEmpleado(@RequestBody Empleado empleado) {

        if (empleadoRepository.findByDocumento(empleado.getDocumento()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(empleadoRepository.save(empleado));
    }

    @GetMapping
    public List<Empleado> listarEmpleados() {
        return empleadoRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empleado> obtenerEmpleado(@PathVariable Long id) {

        return empleadoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Empleado> actualizarEmpleado(
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
    public ResponseEntity<Void> eliminarEmpleado(@PathVariable Long id) {

        if (!empleadoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        empleadoRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}