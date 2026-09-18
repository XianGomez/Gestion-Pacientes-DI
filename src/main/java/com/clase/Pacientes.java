package com.clase;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

public class Pacientes implements Initializable {

    @FXML
    private TextField dnipac, apelpac, nompac, tlfpac, emailpac, dirpac, tlfopac;
    @FXML
    private DatePicker nacpac;
    @FXML 
    private ComboBox<String> cmbpac, locpac;
    @FXML 
    private Button btnguardarpac, btnmodifpac, btndelpac;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cmbpac.getItems().addAll("A Coruña", "Lugo", "Ourense", "Pontevedra");
        locpac.getItems().addAll("Vigo", "Pontevedra", "Tui", "Santiago", "Ferrol");

        dnipac.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { 
                comprobarDni();
            }
        });
    }

    @FXML
    private void comprobarDni() {
        String dni = dnipac.getText() != null ? dnipac.getText().trim().toUpperCase() : "";
        
        if (dni.isEmpty()) {
            
            return;
        }
        
        if (validarDniNie(dni)) {
            dnipac.setStyle(""); 
            dnipac.setText(dni);
        } else {
            
            dnipac.setStyle("-fx-border-color: red; -fx-border-width: 1.5px;");
            dnipac.setText(""); 
        }
    }

    private boolean validarDniNie(String documento) {
        if (documento.matches("\\d{8}[A-Z]")) {
            int numero = Integer.parseInt(documento.substring(0, 8));
            char letra = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(numero % 23);
            return letra == documento.charAt(8);
        }

        if (documento.matches("[XYZ]\\d{7}[A-Z]")) {
            String nie = documento
                    .replace("X", "0")
                    .replace("Y", "1")
                    .replace("Z", "2");

            int numero = Integer.parseInt(nie.substring(0, 8));
            char letra = "TRWAGMYFPDXBNJZSQVHLCKE".charAt(numero % 23);
            return letra == documento.charAt(8);
        }

        return false;
    }

    @FXML 
    private void guardarPaciente() {
        String dni = dnipac.getText();
        String apellidos = apelpac.getText();
        String nombre = nompac.getText();

        LocalDate fechaNacimiento = nacpac.getValue();

        String telefono = tlfopac.getText();
        String email = emailpac.getText();
        String direccion = dirpac.getText();

        String provincia = cmbpac.getValue();
        String localidad = locpac.getValue();

        System.out.println("=======PACIENTE=========");
        System.out.println("DNI: " + dni);
        System.out.println("Apellidos: " + apellidos);
        System.out.println("Nombre: " + nombre);
        System.out.println("Fecha Nacimiento: " + fechaNacimiento);
        System.out.println("Telefono: " + telefono);
        System.out.println("Email: " + email);
        System.out.println("Direccion: " + direccion);
        System.out.println("Provincia: " + provincia);
        System.out.println("Localidad: " + localidad);
        System.out.println("========================");
    }
}