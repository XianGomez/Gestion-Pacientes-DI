package com.clase.modelo;

import java.time.LocalDate;

public class Cita {
    private int idcita;
    private final String dniPaciente;
    private final int idDoctor;
    private final LocalDate fecha;
    private final String hora;
    private final String motivo;
    private final String diagnostico;
    private final String tratamiento;
    private final String estado;
    private final String doctor;
    private final String especialidad;

    public Cita(int idcita, String dniPaciente, int idDoctor, LocalDate fecha, String hora,
            String motivo, String diagnostico, String tratamiento, String estado) {
        this(idcita, dniPaciente, idDoctor, fecha, hora, motivo, diagnostico, tratamiento, estado, "", "");
    }

    public Cita(int idcita, String dniPaciente, int idDoctor, LocalDate fecha, String hora,
            String motivo, String diagnostico, String tratamiento, String estado,
            String doctor, String especialidad) {
        this.idcita = idcita;
        this.dniPaciente = dniPaciente;
        this.idDoctor = idDoctor;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.estado = estado;
        this.doctor = doctor;
        this.especialidad = especialidad;
    }

    public int getIdcita() {
        return idcita;
    }

    public void setIdcita(int idcita) {
        this.idcita = idcita;
    }

    public String getDniPaciente() {
        return dniPaciente;
    }

    public int getIdDoctor() {
        return idDoctor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public String getEstado() {
        return estado;
    }

    public String getDoctor() {
        return doctor;
    }

    public String getEspecialidad() {
        return especialidad;
    }
}
