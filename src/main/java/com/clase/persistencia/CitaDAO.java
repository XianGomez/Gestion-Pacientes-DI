package com.clase.persistencia;

import java.util.List;

import com.clase.modelo.Cita;

public interface CitaDAO {
    List<Cita> cargarCitas();
    Cita buscarCita(int idCita);
    List<Cita> cargarCitaspaciente(String dniPaciente);
    int guardarCita(Cita cita);
    int modificarCita(Cita cita);
    int eliminaCita(int idCita);
}
