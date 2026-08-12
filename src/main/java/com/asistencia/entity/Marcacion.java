package com.asistencia.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "marcaciones")
public class Marcacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    private TipoMarcacion tipo;

   
    private Integer minutosRetraso = 0;

    @ManyToOne
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    public Long getId() {
        return id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public TipoMarcacion getTipo() {
        return tipo;
    }

    public void setTipo(TipoMarcacion tipo) {
        this.tipo = tipo;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }
    public Integer getMinutosRetraso() {
        return minutosRetraso;
    }

    public void setMinutosRetraso(Integer minutosRetraso) {
        this.minutosRetraso = minutosRetraso;
    }
}