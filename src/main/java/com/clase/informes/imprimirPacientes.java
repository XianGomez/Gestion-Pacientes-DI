package com.clase.informes;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.clase.persistencia.ConexionMySQL;

import javafx.scene.control.Alert;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;



public class imprimirPacientes {


    // =====================================================
    // MÉTIDOD MOSTRAR EL INFORME DE CLIENTES
    // =====================================================
    public static void mostrar() {
        try (Connection conexion = ConexionMySQL.getConexion()) {
            if (conexion == null) {
                throw new Exception("No se pudo conectar con la base de datos.");
            }

            try (InputStream informe = imprimirPacientes.class.getResourceAsStream(
                    "/informes/pacientes.jrxml");
                 InputStream logo = imprimirPacientes.class.getResourceAsStream(
                    "/img/salud.png")) {

                if (informe == null) {
                    throw new Exception("No se encuentra pacientes.jrxml en resources.");
                }
                if (logo == null) {
                    throw new Exception("No se encuentra salud.png en resources.");
                }

                JasperReport jasperReport =
                    JasperCompileManager.compileReport(informe);

                Map<String, Object> parametros = new HashMap<>();
                parametros.put("LOGO", logo);

                JasperPrint jasperPrint =
                    JasperFillManager.fillReport(
                        jasperReport,
                        parametros,
                        conexion
                    );

                JasperViewer.viewReport(jasperPrint, false);
            }
        } catch (Exception e) {
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al generar el informe");
            alert.setHeaderText("No se ha podido generar el informe de pacientes");
            alert.setContentText(e.getMessage() != null
                ? e.getMessage()
                : e.getClass().getSimpleName());
            alert.showAndWait();
        }
    }
}
