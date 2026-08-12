package com.asistencia.entity;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "horarios")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Ej: "Turno Mañana", "Oficina"
    
    private LocalTime horaEntrada; // Ej: 08:00
    
    private LocalTime horaSalida;  // Ej: 17:00
    
    private Integer toleranciaMinutos; // Ej: 15 minutos de gracia

    // Constructor vacío requerido por JPA
    public Horario() {}

    public Horario(String nombre, LocalTime horaEntrada, LocalTime horaSalida, Integer toleranciaMinutos) {
        this.nombre = nombre;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
        this.toleranciaMinutos = toleranciaMinutos;
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Integer getToleranciaMinutos() {
        return toleranciaMinutos;
    }

    public void setToleranciaMinutos(Integer toleranciaMinutos) {
        this.toleranciaMinutos = toleranciaMinutos;
    }
}