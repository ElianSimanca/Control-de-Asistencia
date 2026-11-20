package com.asistencia.dto;

import java.time.OffsetDateTime;

public class AttendanceRequest {
    private String uid;
    private Long empleadoId;
    private String tipo; // "ENTRADA" o "SALIDA"
    private OffsetDateTime fechaHora; // Fecha y hora exacta de la marcación

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public Long getEmpleadoId() { return empleadoId; }
    public void setEmpleadoId(Long empleadoId) { this.empleadoId = empleadoId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public OffsetDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(OffsetDateTime fechaHora) { this.fechaHora = fechaHora; }
}
