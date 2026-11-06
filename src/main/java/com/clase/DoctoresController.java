package com.clase;

import com.clase.modelo.Doctor;
import com.clase.persistencia.DoctorDAOMySQL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;

public class DoctoresController implements Initializable {

    @FXML
    private TextField iddoc, apeldoc, nomedoc, movildoc, emaildoc;
    @FXML 
    private ComboBox<String> espedoc;
    @FXML
    private RadioButton coledocSi, coledocNo;
    @FXML
    private TableView<Doctor> tablaDoctores;
    @FXML
    private TableColumn<Doctor, String> coliddoc, colapeldoc, colnomedoc, colmovildoc, colespedoc;

    private final ObservableList<Doctor> doctores = FXCollections.observableArrayList();
    private boolean doctorexiste;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarEspecialidades();
        espedoc.setEditable(true);
        tablaDoctores.setItems(doctores);

        ToggleGroup grupoColegiado = new ToggleGroup();
        coledocSi.setToggleGroup(grupoColegiado);
        coledocNo.setToggleGroup(grupoColegiado);

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

    private void cargarDoctor(Doctor doctor) {
        if (doctor == null) {
            return;
        }

        doctorexiste = true;
        iddoc.setText(doctor.getIddoc());
        apeldoc.setText(doctor.getApeldoc());
        nomedoc.setText(doctor.getNomedoc());
        getCampoTelefono().setText(doctor.getMovildoc());
        emaildoc.setText(doctor.getEmail());
        if (doctor.getColedoc() == null) {
            coledocSi.setSelected(false);
            coledocNo.setSelected(false);
        } else {
            coledocSi.setSelected(doctor.getColedoc());
            coledocNo.setSelected(!doctor.getColedoc());
        }
        espedoc.setValue(doctor.getEspedoc());
    }

    private void cargarEspecialidades() {
        try (InputStream entrada = getClass().getResourceAsStream("/com/clase/data/especialidades.json")) {
            if (entrada == null) {
                throw new IOException("No se encontró especialidades.json");
            }

            JsonObject datos = JsonParser.parseReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            JsonArray especialidades = datos.has("especialidades")
                    ? datos.getAsJsonArray("especialidades")
                    : datos.getAsJsonArray("provincias");

            List<String> nombres = Arrays.stream(new Gson().fromJson(especialidades, Provincia[].class))
                    .map(Provincia::getNm)
                    .collect(Collectors.toList());

            espedoc.getItems().setAll(nombres);
        } catch (IOException | RuntimeException e) {
            espedoc.getItems().clear();
            System.err.println("ERROR al cargar las especialidades: " + e.getMessage());
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
        String id = iddoc.getText() != null ? iddoc.getText().trim() : "";
        
        if (id.isEmpty()) {
            iddoc.setStyle("");
            return;
        }
        
        if (id.matches("\\d+")) {
            iddoc.setStyle("-fx-border-color: green; -fx-border-width: 1.5px;");
        } else {
            iddoc.setStyle("-fx-border-color: red; -fx-border-width: 1.5px;");
        }
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
    private void guardarDoctor() {
        String id = iddoc.getText() != null ? iddoc.getText().trim() : "";
        String apellidos = apeldoc.getText() != null ? apeldoc.getText().trim() : "";
        String nombre = nomedoc.getText() != null ? nomedoc.getText().trim() : "";
        String movil = movildoc.getText() != null ? movildoc.getText().trim() : "";
        String email = emaildoc.getText() != null ? emaildoc.getText().trim() : "";
        String especialidad = espedoc.getValue();
        Boolean colegiado = coledocSi.isSelected() ? Boolean.TRUE
                : coledocNo.isSelected() ? Boolean.FALSE : null;

        if (apellidos.isBlank() || nombre.isBlank() || movil.isBlank()
                || especialidad == null || colegiado == null) {
            System.err.println("No se puede guardar: completa todos los campos obligatorios");
            return;
        }

        Doctor doctor = new Doctor(id, apellidos, nombre, movil, email, colegiado, especialidad);

        DoctorDAOMySQL dao = new DoctorDAOMySQL();
        if (doctorexiste) {
            dao.modificarDoctor(doctor.getIddoc(), doctor);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText("Sanidad Teis");
            alert.setContentText("Doctor Modificado Correctamente");
            alert.showAndWait();

        } else {
            dao.guardarDoctor(doctor);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText("Sanidad Teis");
            alert.setContentText("Doctor Guardado Correctamente");
            alert.showAndWait();
        }

        doctorexiste = false;
        cargarDoctores();
        iddoc.setText(doctor.getIddoc());
    }

    @FXML
    private void guardarPaciente() {
        guardarDoctor();
    }

    public void cargarDoctor() {
        cargarDoctor(tablaDoctores.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void limpiarCampos() {
        doctorexiste = false;
        iddoc.clear();
        apeldoc.clear();
        nomedoc.clear();
        movildoc.clear();
        emaildoc.clear();
        coledocSi.setSelected(false);
        coledocNo.setSelected(false);
        espedoc.setValue(null);
        tablaDoctores.getSelectionModel().clearSelection();
    }

    @FXML 
    private void cargarDoctores() {
        DoctorDAOMySQL dao = new DoctorDAOMySQL();
        List<Doctor> doctores = dao.cargarDoctores();
        tablaDoctores.getItems().setAll(doctores);
    }

    @FXML
    public void eliminarDoctor() {
        Doctor seleccionado = tablaDoctores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        DoctorDAOMySQL dao = new DoctorDAOMySQL();
        try {

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Quieres eliminar al doctor?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                dao.eliminarDoctor(seleccionado.getIddoc());
                tablaDoctores.getItems().remove(seleccionado);
                limpiarCampos();
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                alert2.setTitle("Información");
                alert2.setHeaderText("Sanidad Teis");
                alert2.setContentText("Doctor Eliminado Correctamente");
                alert2.showAndWait();
                
            }
            
        } catch (IllegalStateException e) {
            System.err.println("ERROR al eliminar el doctor: " + e.getMessage());
        }
    }

    @FXML
    private void buscaDocId() {
        DoctorDAOMySQL dao = new DoctorDAOMySQL();
        Doctor doctor = dao.buscaDocId(iddoc.getText());
        if (doctor == null) {
            System.err.println("No se encontró un doctor con ese ID");
            return;
        }

        cargarDoctor(doctor);
    }

    @FXML
    public void buscadocporEspecialidad() {
        String especialidad = espedoc.getValue();
        if (especialidad == null || especialidad.isBlank()) {
            System.err.println("Selecciona una especialidad para buscar doctores");
            return;
        }

        DoctorDAOMySQL dao = new DoctorDAOMySQL();
        List<Doctor> doctoresEncontrados = dao.cargarDoctoresEspec(especialidad);
        tablaDoctores.getItems().setAll(doctoresEncontrados);
        if (doctoresEncontrados.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Búsqueda de doctores");
            alert.setHeaderText(null);
            alert.setContentText("No hay doctores con la especialidad " + especialidad + ".");
            alert.showAndWait();
        }
    }

}
