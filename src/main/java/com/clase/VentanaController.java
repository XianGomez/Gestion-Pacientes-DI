package com.clase;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.clase.informes.imprimirPacientes;

public class VentanaController {
    @FXML
    private PacientesController pacientesController;
    @FXML
    private DoctoresController doctoresController;
    @FXML
    private CitasController citasController;
    @FXML
    private TabPane tabPanePrincipal;
    @FXML
    private Tab tabCitas;

    @FXML
    private void initialize() {
        pacientesController.setAlSeleccionarPaciente(citasController::seleccionarPaciente);
        doctoresController.setAlSeleccionarDoctor(citasController::seleccionarDoctor);
        tabPanePrincipal.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, seleccionada) -> {
                    if (seleccionada == tabCitas) {
                        citasController.mostrarDatosSeleccionados();
                    }
                });
    }

    @FXML
    private void informePacientes() {
        imprimirPacientes.mostrar();
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
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
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
