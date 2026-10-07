package com.clase;

import com.clase.modelo.Doctor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;



public class DoctoresController implements Initializable {

    @FXML
    private TextField iddoc, apeldoc, nomedoc, movildoc, emaildoc;
    @FXML 
    private ComboBox<String> espedoc;
    @FXML
    private TableView<Doctor> tablaDoctores;
    @FXML
    private TableColumn<Doctor, String> coliddoc, colapeldoc, colnomedoc, colmovildoc, colespedoc;

    private final ObservableList<Doctor> doctores = FXCollections.observableArrayList();
    private Doctor doctorSeleccionado;
    private boolean doctorexiste;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarEspecialidades();
        espedoc.setEditable(true);
        tablaDoctores.setItems(doctores);

        iddoc.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { 
                comprobarId();
            }
        });

        nomedoc.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && nomedoc.getText() != null) {
                nomedoc.setText(formatearNombrePropio(nomedoc.getText()));
            }
        });

        apeldoc.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && apeldoc.getText() != null) {
                apeldoc.setText(formatearNombrePropio(apeldoc.getText()));
            }
        });

        movildoc.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                comprobarTelefono();
            }
        });

        coliddoc.setCellValueFactory(new PropertyValueFactory<>("iddoc"));
        colapeldoc.setCellValueFactory(new PropertyValueFactory<>("apeldoc"));
        colnomedoc.setCellValueFactory(new PropertyValueFactory<>("nomedoc"));
        colmovildoc.setCellValueFactory(new PropertyValueFactory<>("movildoc"));
        colespedoc.setCellValueFactory(new PropertyValueFactory<>("espedoc"));
        cargarDoctores();

        tablaDoctores.getSelectionModel().selectedItemProperty().addListener((observable, anterior, nuevo) -> {
            if (nuevo != null) {
                cargarDoctor(nuevo);
            }
        });
    }

    private void cargarEspecialidades() {
        try (InputStream entrada = getClass().getResourceAsStream("/com/clase/data/especialidades.json")) {
            if (entrada == null) {
                throw new IOException("No se encontró especialidades.json");
            }

            JsonObject datos = JsonParser.parseReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            Provincia[] provincias = new Gson().fromJson(datos.getAsJsonArray("provincias"), Provincia[].class);

            espedoc.getItems().setAll(Arrays.stream(provincias)
                    .map(Provincia::getNm)
                    .collect(Collectors.toList()));
        } catch (IOException | RuntimeException e) {
            espedoc.getItems().clear();
            System.err.println("ERROR al cargar las provincias: " + e.getMessage());
        }
    }

    
    private TextField getCampoTelefono() {
        return movildoc != null ? movildoc : movildoc;
    }

    private String formatearNombrePropio(String texto) {
        texto = texto.trim();
        if (texto.isEmpty()) {
            return "";
        }

        String[] palabras = texto.split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                         .append(palabra.substring(1).toLowerCase())
                         .append(" ");
            }
        }

        return resultado.toString().trim();
    }

    @FXML
    private void comprobarId() {
        String id = iddoc.getText() != null ? iddoc.getText().trim().toUpperCase() : "";
        
        if (id.isEmpty()) {
            iddoc.setStyle("");
            return;
        }
        
        if (validarDniNie(id)) {
            iddoc.setStyle("-fx-border-color: green; -fx-border-width: 1.5px;");
        } else {
            iddoc.setStyle("-fx-border-color: red; -fx-border-width: 1.5px;");
            iddoc.setText("");
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
    private void comprobarTelefono() {
        TextField campoTlf = getCampoTelefono();
        if (campoTlf == null) return;

        String tlf = campoTlf.getText() != null ? campoTlf.getText().trim() : "";

        if (tlf.isEmpty()) {
            campoTlf.setStyle("");
            return;
        }

        if (validarTelefono(tlf)) {
            campoTlf.setStyle("-fx-border-color: green; -fx-border-width: 1.5px;");
        } else {
            campoTlf.setStyle("-fx-border-color: red; -fx-border-width: 1.5px;");
            campoTlf.setText("");
        }
    }

    
    private boolean validarTelefono(String telefono) {
        return telefono != null && telefono.matches("^[6789]\\d{8}$");
    }

    @FXML
    private void guardarPaciente() {
        

        String id = iddoc.getText().toUpperCase();
        String apellidos = apeldoc.getText();
        String nombre = nomedoc.getText();
        String movil = movildoc.getText();
        String email = emaildoc.getText();
        String especialidad = espedoc.getValue();
        // Creamos el objeto Paciente 
        if (id.isBlank() || apellidos.isBlank() || nombre.isBlank() || movil.isBlank()
                || especialidad == null) {
            System.err.println("No se puede guardar: completa todos los campos obligatorios");
            return;
        }

        Doctor doctor = new Doctor(id, apellidos, nombre, movil, email, especialidad);

        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        if (doctorexiste) {
            dao.modificarDoctor(doctor.getIddoc(), doctor);
            doctorexiste = false;
            buscaPacDni();
        } else {
            dao.guardarDoctor(doctor);
        }

        cargarDoctores();
    }

    public void cargarDoctor() {
        Doctor doctor = tablaDoctores.getSelectionModel().getSelectedItem();
        if (doctor == null) {
            return;
        }

        doctorexiste = true;
        iddoc.setText(doctor.getIddoc());
        apeldoc.setText(doctor.getApeldoc());
        nomedoc.setText(doctor.getNomedoc());
        getCampoTelefono().setText(doctor.getMovildoc());
        emaildoc.setText(doctor.getMaildoc());
        espedoc.setValue(doctor.getEspedoc());


    }

    @FXML
    private void limpiarCampos() {
        doctorexiste = false;
        iddoc.clear();
        apeldoc.clear();
        nomedoc.clear();
        movildoc.clear();
        emaildoc.clear();
        espedoc.setValue(null);
        tablaDoctores.getSelectionModel().clearSelection();
    }

    @FXML 
    private void cargarDoctores() {
        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        List<Doctor> doctores = dao.cargarDoctores();
        tablaDoctores.getItems().setAll(doctores);
    }

    @FXML 
    public void eliminarDoctor() {
        Doctor seleccionado = tablaDoctores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        try {
            dao.eliminarDoctor(seleccionado.getIddoc());
            tablaDoctores.getItems().remove(seleccionado);
            limpiarCampos();
        } catch (IllegalStateException e) {
            System.err.println("ERROR al eliminar el paciente: " + e.getMessage());
        }
    }

    @FXML
    private void buscaDocId() {

        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        Doctor doctor = dao.buscaDocId(iddoc.getText());
        if (doctor == null) {
            System.err.println("No se encontró un paciente con ese DNI");
            return;
        }

        iddoc.setText(doctor.getIddoc());
        apeldoc.setText(doctor.getApeldoc());
        nomedoc.setText(doctor.getNomedoc());
        movildoc.setText(doctor.getMovildoc());
        emaildoc.setText(doctor.getMaildoc());
        espedoc.setValue(doctor.getEspedoc());

    }

}
