package com.clase.persistencia;

import java.util.List;

import com.clase.modelo.Doctor;

public interface DoctorDAO {
    void guardarDoctor(Doctor doctor);
    List<Doctor> cargarDoctores();
    List<Doctor> cargarDoctoresEspec(String especialidad);
    void modificarDoctor(Integer id, Doctor doctor);
}
