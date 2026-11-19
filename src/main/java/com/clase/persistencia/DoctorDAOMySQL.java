package com.clase.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.clase.modelo.Doctor;

public class DoctorDAOMySQL implements DoctorDAO {

    private void crearTablaSiNoExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS doctores ("
                + "iddoc INT NOT NULL AUTO_INCREMENT PRIMARY KEY, "
                + "apeldoc VARCHAR(128) NOT NULL, "
                + "nomedoc VARCHAR(128) NOT NULL, "
                + "movildoc VARCHAR(15) NOT NULL, "
                + "maildoc VARCHAR(128), "
                + "coledoc TINYINT(1) NOT NULL, "
                + "espedoc VARCHAR(128) NOT NULL)";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al preparar la tabla de doctores: " + e.getMessage());
        }
    }

    @Override
    public void guardarDoctor(Doctor doctor) {
        crearTablaSiNoExiste();

        String sql = "INSERT INTO doctores (apeldoc, nomedoc, movildoc, maildoc, coledoc, espedoc) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, doctor.getApeldoc());
            ps.setString(2, doctor.getNomedoc());
            ps.setString(3, doctor.getMovildoc());
            ps.setString(4, doctor.getEmail());
            ps.setBoolean(5, Boolean.TRUE.equals(doctor.getColedoc()));
            ps.setString(6, doctor.getEspedoc());
            ps.executeUpdate();

            System.out.println("Doctor guardado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al guardar el doctor: " + e.getMessage());
        }
    }

    @Override
    public List<Doctor> cargarDoctores() {
        crearTablaSiNoExiste();
        List<Doctor> doctores = new ArrayList<>();

        String sql = "SELECT iddoc, apeldoc, nomedoc, movildoc, maildoc, coledoc, espedoc "
                + "FROM doctores ORDER BY apeldoc, nomedoc";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Doctor doctor = new Doctor(
                        rs.getString("iddoc"),
                        rs.getString("apeldoc"),
                        rs.getString("nomedoc"),
                        rs.getString("movildoc"),
                        rs.getString("maildoc"),
                        rs.getBoolean("coledoc"),
                        rs.getString("espedoc"));
                doctores.add(doctor);
            }
        } catch (SQLException e) {
            System.out.println("Error al cargar doctores: " + e.getMessage());
        }

        return doctores;
    }

    public Doctor buscaDocId(String iddoc) {
        crearTablaSiNoExiste();
        String sql = "SELECT iddoc, apeldoc, nomedoc, movildoc, maildoc, coledoc, espedoc "
                + "FROM doctores WHERE iddoc = ?";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, iddoc);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Doctor(
                            rs.getString("iddoc"),
                            rs.getString("apeldoc"),
                            rs.getString("nomedoc"),
                            rs.getString("movildoc"),
                            rs.getString("maildoc"),
                            rs.getBoolean("coledoc"),
                            rs.getString("espedoc"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar el doctor: " + e.getMessage());
        }

        return null;
    }

    public void eliminarDoctor(String iddoc) {
        crearTablaSiNoExiste();
        String sql = "DELETE FROM doctores WHERE iddoc = ?";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, iddoc);
            ps.executeUpdate();
            System.out.println("Doctor eliminado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error eliminar doctor: " + e.getMessage());
        }
    }

    public void modificarDoctor(String iddoc, Doctor doctor) {
        crearTablaSiNoExiste();
        String sql = "UPDATE doctores SET apeldoc = ?, nomedoc = ?, movildoc = ?, "
                + "maildoc = ?, coledoc = ?, espedoc = ? WHERE iddoc = ?";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, doctor.getApeldoc());
            ps.setString(2, doctor.getNomedoc());
            ps.setString(3, doctor.getMovildoc());
            ps.setString(4, doctor.getEmail());
            ps.setBoolean(5, Boolean.TRUE.equals(doctor.getColedoc()));
            ps.setString(6, doctor.getEspedoc());
            ps.setString(7, iddoc);
            ps.executeUpdate();
            System.out.println("Doctor modificado correctamente.");
        } catch (SQLException e) {
            System.out.println("Error al modificar el doctor: " + e.getMessage());
        }
    }

    @Override
    public void modificarDoctor(Integer id, Doctor doctor) {
        modificarDoctor(Objects.requireNonNull(id, "El ID del doctor no puede ser nulo").toString(), doctor);
    }

    @Override
    public List<Doctor> cargarDoctoresEspec(String espedoc) {
        crearTablaSiNoExiste();
        List<Doctor> doctores = new ArrayList<>();
        String sql = "SELECT iddoc, apeldoc, nomedoc, movildoc, maildoc, coledoc, espedoc "
                + "FROM doctores WHERE espedoc = ?";

        try (Connection conexion = ConexionMySQL.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, espedoc);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    doctores.add(new Doctor(
                            rs.getString("iddoc"),
                            rs.getString("apeldoc"),
                            rs.getString("nomedoc"),
                            rs.getString("movildoc"),
                            rs.getString("maildoc"),
                            rs.getBoolean("coledoc"),
                            rs.getString("espedoc")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar doctores por especialidad: " + e.getMessage());
        }

        return doctores;
    }
}
