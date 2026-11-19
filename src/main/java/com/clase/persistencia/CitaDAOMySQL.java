package com.clase.persistencia;

import com.clase.modelo.Cita;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CitaDAOMySQL implements CitaDAO {
    public List<Cita> cargarCitas() {
        String sql = "SELECT idcita, citadnipac, citadoc, fechacita, horacita, motivo, "
                + "diagnostico, tratamiento, estadocita FROM citas ORDER BY fechacita, horacita";
        List<Cita> citas = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                citas.add(new Cita(
                        rs.getInt("idcita"),
                        rs.getString("citadnipac"),
                        rs.getInt("citadoc"),
                        rs.getDate("fechacita").toLocalDate(),
                        rs.getString("horacita"),
                        rs.getString("motivo"),
                        rs.getString("diagnostico"),
                        rs.getString("tratamiento"),
                        rs.getString("estadocita")));
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar las citas: " + e.getMessage());
        }

        return citas;
    }

    public Cita buscarCita(int idCita) {
        String sql = "SELECT idcita, citadnipac, citadoc, fechacita, horacita, motivo, "
                + "diagnostico, tratamiento, estadocita FROM citas WHERE idcita = ?";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Cita(
                            rs.getInt("idcita"),
                            rs.getString("citadnipac"),
                            rs.getInt("citadoc"),
                            rs.getDate("fechacita").toLocalDate(),
                            rs.getString("horacita"),
                            rs.getString("motivo"),
                            rs.getString("diagnostico"),
                            rs.getString("tratamiento"),
                            rs.getString("estadocita"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar cita por ID: " + e.getMessage());
        }

        return null;
    }

    public List<Cita> cargarCitaspaciente(String dniPaciente) {
        String sql = "SELECT c.idcita, c.citadnipac, c.citadoc, c.fechacita, c.horacita, "
                + "c.motivo, c.diagnostico, c.tratamiento, c.estadocita, "
                + "CONCAT(d.apeldoc, ', ', d.nomdoc) AS doctor, d.espedoc "
                + "FROM citas c JOIN doctores d ON d.iddoc = c.citadoc "
                + "WHERE c.citadnipac = ? ORDER BY c.fechacita, c.horacita";
        List<Cita> citas = new ArrayList<>();

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dniPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    citas.add(new Cita(
                            rs.getInt("idcita"),
                            rs.getString("citadnipac"),
                            rs.getInt("citadoc"),
                            rs.getDate("fechacita").toLocalDate(),
                            rs.getString("horacita"),
                            rs.getString("motivo"),
                            rs.getString("diagnostico"),
                            rs.getString("tratamiento"),
                            rs.getString("estadocita"),
                            rs.getString("doctor"),
                            rs.getString("espedoc")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar citas paciente: " + e.getMessage());
        }

        return citas;
    }

    public int guardarCita(Cita cita) {
        String sql = "INSERT INTO citas "
                + "(citadnipac, citadoc, fechacita, horacita, motivo, diagnostico, tratamiento, estadocita) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarDatos(ps, cita);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    return claves.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            System.out.println("Error al guardar la cita: " + e.getMessage());
            return 0;
        }
    }

    public int modificarCita(Cita cita) {
        String sql = "UPDATE citas SET citadnipac = ?, citadoc = ?, fechacita = ?, horacita = ?, "
                + "motivo = ?, diagnostico = ?, tratamiento = ?, estadocita = ? WHERE idcita = ?";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignarDatos(ps, cita);
            ps.setInt(9, cita.getIdcita());
            int filas = ps.executeUpdate();
            System.out.println("Cita modificada correctamente.");
            return filas;
        } catch (SQLException e) {
            System.out.println("Error al modificar la cita: " + e.getMessage());
            return 0;
        }
    }

    public int eliminaCita(int idCita) {
        String sql = "DELETE FROM citas WHERE idcita = ?";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCita);
            int filas = ps.executeUpdate();
            System.out.println("Cita eliminada correctamente.");
            return filas;
        } catch (SQLException e) {
            System.out.println("Error al eliminar la cita: " + e.getMessage());
            return 0;
        }
    }

    private void asignarDatos(PreparedStatement ps, Cita cita) throws SQLException {
        ps.setString(1, cita.getDniPaciente());
        ps.setInt(2, cita.getIdDoctor());
        ps.setDate(3, java.sql.Date.valueOf(cita.getFecha()));
        ps.setString(4, cita.getHora());
        setNullableString(ps, 5, cita.getMotivo());
        setNullableString(ps, 6, cita.getDiagnostico());
        setNullableString(ps, 7, cita.getTratamiento());
        ps.setString(8, cita.getEstado());
    }

    private void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value.trim());
        }
    }

}
