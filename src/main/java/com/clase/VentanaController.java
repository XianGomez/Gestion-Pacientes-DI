package com.clase;

import com.clase.modelo.Paciente;
import com.clase.persistencia.PacienteDAOMySQL;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class VentanaController {
    @FXML
    private AnchorPane rootVentana;
    @FXML
    private PacientesController pacientesController;
    @FXML
    private DoctoresController doctoresController;
    @FXML
    private CitasController citasController;

    @FXML
    private void initialize() {
        pacientesController.setAlSeleccionarPaciente(citasController::seleccionarPaciente);
        doctoresController.setAlSeleccionarDoctor(citasController::seleccionarDoctor);
    }

    @FXML 
    private void salirApp() {
        Platform.exit();
    }

    @FXML 
    private void mostrarAcercade() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/clase/acercade.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("Acerca De");
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void exportarPacientesPDF() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Exportar pacientes a PDF");
        selector.setInitialFileName("pacientes.pdf");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Documento PDF", "*.pdf"));
        File archivo = selector.showSaveDialog(rootVentana.getScene().getWindow());
        if (archivo == null) {
            return;
        }
        if (!archivo.getName().toLowerCase().endsWith(".pdf")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".pdf");
        }

        try {
            List<Paciente> pacientes = new PacienteDAOMySQL().cargarPacientes();
            InformePacientesPDF.exportar(archivo, pacientes);
            Alert alerta = new Alert(AlertType.INFORMATION,
                    "Se exportaron " + pacientes.size() + " pacientes a:\n" + archivo.getAbsolutePath(),
                    ButtonType.OK);
            alerta.setTitle("Informe de pacientes");
            alerta.setHeaderText("Exportación completada");
            alerta.showAndWait();
        } catch (IllegalStateException | IOException e) {
            Alert alerta = new Alert(AlertType.ERROR);
            alerta.setTitle("Informe de pacientes");
            alerta.setHeaderText("No se pudo exportar el informe.");
            alerta.setContentText(detalle(e));
            alerta.showAndWait();
        }
    }

    private String detalle(Exception e) {
        Throwable causa = e.getCause() == null ? e : e.getCause();
        return causa.getMessage() == null ? causa.toString() : causa.getMessage();
    }

    @FXML 
    private void abrirDir() {
        try {
            new ProcessBuilder("explorer.exe").start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
