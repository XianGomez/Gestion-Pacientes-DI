package com.clase.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Cita {
    private final int idcita;
    private final String dniPaciente;
    private final int idDoctor;
    private final LocalDate fecha;
    private final LocalTime hora;
    private final String motivo;
    private final String diagnostico;
    private final String tratamiento;
    private final String estado;

    public Cita(int idcita, String dniPaciente, int idDoctor, LocalDate fecha, LocalTime hora,
            String motivo, String diagnostico, String tratamiento, String estado) {
        this.idcita = idcita;
        this.dniPaciente = dniPaciente;
        this.idDoctor = idDoctor;
        this.fecha = fecha;
        this.hora = hora;
        this.motivo = motivo;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.estado = estado;
    }

    public int getIdcita() {
        return idcita;
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

    public LocalTime getHora() {
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
}
