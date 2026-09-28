package com.clase.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.clase.modelo.Paciente;

public class PacienteDAOMySQL implements PacienteDAO{

    @Override 
    public void guardarPaciente(Paciente paciente) {
        String sql = "INSERT INTO pacientes "
                + "(dnipac, apelpac, nompac, nacimientopac, movilpac, mailpac, dirpac, propac, munipac) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionMySQL.getConexion();
            PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, paciente.getDni());
                ps.setString(2, paciente.getApellidos());
                ps.setString(3, paciente.getNombre());
                ps.setDate(4, java.sql.Date.valueOf(paciente.getNacimiento()));
                ps.setString(5, paciente.getMovil());
                ps.setString(6, paciente.getEmail());
                ps.setString(7, paciente.getDireccion());
                ps.setString(8, paciente.getProvincia());
                ps.setString(9, paciente.getMunicipio());

                ps.executeUpdate();

                System.out.println("Paciente guardado correctamente");

            } catch (SQLException e) {
                throw new IllegalStateException("No se pudo guardar el paciente en la base de datos", e);
            }
    }

    @Override
    public List<Paciente> cargarPacientes() {

        List<Paciente> pacientes = new ArrayList<>();

        // Solo obtenemos los campos que necesitamos para la tabla
        String sql = "SELECT dnipac, apelpac, nompac, nacimientopac, movilpac, "
            + "mailpac, dirpac, propac, munipac "
                + "FROM pacientes "
                + "ORDER BY apelpac, nompac";

        try (Connection conexion = ConexionMySQL.getConexion();
                PreparedStatement ps = conexion.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            // Recorremos las filas obtenidas
            while (rs.next()) {

                Paciente paciente = new Paciente(
                        rs.getString("dnipac"),
                        rs.getString("apelpac"),
                        rs.getString("nompac"),
                        rs.getDate("nacimientopac").toLocalDate(),
                        rs.getString("movilpac"),
                        rs.getString("mailpac"),
                        rs.getString("dirpac"),
                        rs.getString("propac"),
                        rs.getString("munipac"));

                pacientes.add(paciente);
            }

        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron cargar los pacientes", e);
        }

        return pacientes;
    }

    @Override
    public Paciente buscarPaciente(String dni) {
        String sql = "SELECT dnipac, apelpac, nompac, nacimientopac, movilpac, "
                + "mailpac, dirpac, propac, munipac FROM pacientes WHERE dnipac = ?";

        try (Connection conexion = ConexionMySQL.getConexion();
                PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dni);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Paciente(
                        rs.getString("dnipac"),
                        rs.getString("apelpac"),
                        rs.getString("nompac"),
                        rs.getDate("nacimientopac").toLocalDate(),
                        rs.getString("movilpac"),
                        rs.getString("mailpac"),
                        rs.getString("dirpac"),
                        rs.getString("propac"),
                        rs.getString("munipac"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar el paciente", e);
        }
    }
    
}
