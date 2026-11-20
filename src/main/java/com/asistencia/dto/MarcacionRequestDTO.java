package com.asistencia.dto;

import java.time.OffsetDateTime;

public class MarcacionRequestDTO {
    private Long empleadoId;
    private String tipo;
    private OffsetDateTime fechaHora;

    public Long getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public OffsetDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(OffsetDateTime fechaHora) { this.fechaHora = fechaHora; }
}
