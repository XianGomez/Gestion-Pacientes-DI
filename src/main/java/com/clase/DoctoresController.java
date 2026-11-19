package com.clase;

import com.clase.modelo.Doctor;
import com.clase.persistencia.DoctorDAOMySQL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

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
    private Consumer<Doctor> alSeleccionarDoctor = doctor -> { };
    private boolean doctorexiste;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarEspecialidades();
        espedoc.setEditable(true);
        tablaDoctores.setItems(doctores);

        ToggleGroup grupoColegiado = new ToggleGroup();
        coledocSi.setToggleGroup(grupoColegiado);
        coledocNo.setToggleGroup(grupoColegiado);

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

        emaildoc.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                validarEmail();
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
                alSeleccionarDoctor.accept(nuevo); // enviamos el doctor seleccionado al controlador de citas
                cargarDoctor(nuevo);
            }
        });
    }

    void setAlSeleccionarDoctor(Consumer<Doctor> alSeleccionarDoctor) {
        this.alSeleccionarDoctor = alSeleccionarDoctor;
    }

    private void cargarDoctor(Doctor doctor) {
        if (doctor == null) {
            return;
        }

        coledocNo.setSelected(false);
        coledocSi.setSelected(false);
        doctorexiste = true;

        DoctorDAOMySQL dao = new DoctorDAOMySQL();
        Doctor doctorbbdd = dao.buscaDocId(doctor.getIddoc());
        if (doctorbbdd == null) {
            return;
        }

        iddoc.setText(doctorbbdd.getIddoc());
        apeldoc.setText(doctorbbdd.getApeldoc());
        nomedoc.setText(doctorbbdd.getNomedoc());
        getCampoTelefono().setText(doctorbbdd.getMovildoc());
        emaildoc.setText(doctorbbdd.getEmail());
        if (Boolean.TRUE.equals(doctorbbdd.getColedoc())) {
            coledocSi.setSelected(true);
        } else {
            coledocNo.setSelected(true);
        }
        espedoc.setValue(doctorbbdd.getEspedoc());
    }

    private void cargarEspecialidades() {
        try (InputStream entrada = getClass().getResourceAsStream("/com/clase/data/especialidades.json")) {
            if (entrada == null) {
                throw new IOException("No se encontró especialidades.json");
            }

            JsonObject datos = JsonParser.parseReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            // Obtenemos el array "especialidades" del JSON
            JsonArray especialidades = datos.has("especialidades")
                    ? datos.getAsJsonArray("especialidades")
                    : datos.getAsJsonArray("provincias");

            // Recorremos todas las especialidades y las añadimos al ComboBox
            for (var especialidad : especialidades) {
                JsonObject p = especialidad.getAsJsonObject();
                espedoc.getItems().add(p.get("nm").getAsString());
            }
        } catch (IOException | RuntimeException e) {
            espedoc.getItems().clear();
            System.err.println("ERROR al cargar las especialidades: " + e.getMessage());
        }
    }

    
    private TextField getCampoTelefono() {
        return movildoc != null ? movildoc : movildoc;
    }

    private String formatearNombrePropio(String texto) {
        String[] palabras = texto.toLowerCase().trim().split("\\s+");
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (!palabra.isEmpty()) {
                resultado.append(Character.toUpperCase(palabra.charAt(0)))
                         .append(palabra.substring(1))
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

    /// validar movil
    @FXML
    private void comprobarTelefono() {
        TextField campoTlf = getCampoTelefono();
        if (campoTlf == null) return;

        String tlf = campoTlf.getText() != null ? campoTlf.getText() : "";
        if (tlf.isEmpty())
            return;

        if (validarTelefono(tlf)) {
            campoTlf.setStyle("");
            campoTlf.setText(tlf);
        } else {
            campoTlf.setStyle("-fx-border-color: red;");
            campoTlf.setText("");
        }
    }

    private boolean validarTelefono(String telefono) {
        return telefono != null && telefono.matches("[67][0-9]{8}");
    }

    private boolean validarEmail() {
        String email = emaildoc.getText() != null ? emaildoc.getText().trim() : "";

        if (email.isEmpty()) {
            emaildoc.setStyle("");
            return true;
        }

        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        boolean emailValido = email.matches(regex);

        if (emailValido) {
            emaildoc.setText(email);
            emaildoc.setStyle("");
        } else {
            emaildoc.clear();
            emaildoc.setStyle("-fx-border-color: red;");
        }

        return emailValido;
    }

    // =============== FUNCIONES CON BASE DE DATOS =======================
    @FXML
    private void guardarDoctor() {
        if (!validarEmail()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Email no válido");
            alert.showAndWait();
            return;
        }

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

        // Creamos el objeto Doctor
        Doctor doctor = new Doctor(id, apellidos, nombre, movil, email, colegiado, especialidad);

        if (doctorexiste) {
            // Si el Doctor ya existe, lo actualizamos en la base de datos
            Alert alert3 = new Alert(Alert.AlertType.CONFIRMATION);
            alert3.setTitle("Aviso");
            alert3.setHeaderText(null);
            alert3.setContentText("¿Desea Modificar datos del Doctor?");
            Optional<ButtonType> result2 = alert3.showAndWait();
            if (result2.isPresent() && result2.get() == ButtonType.OK) {
                DoctorDAOMySQL dao = new DoctorDAOMySQL();
                dao.modificarDoctor(Integer.parseInt(id), doctor);
                doctorexiste = false; // importante sino siempre graba
                Alert alert4 = new Alert(Alert.AlertType.INFORMATION);
                alert4.setTitle("Aviso");
                alert4.setHeaderText(null);
                alert4.setContentText("Doctor modificado en Base de Datos");
                alert4.showAndWait();
            } else {
                return;
            }
        } else {
            // Si el Doctor no existe, lo guardamos en la base de datos
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("¿Desea Grabar el Doctor?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                DoctorDAOMySQL dao = new DoctorDAOMySQL();
                dao.guardarDoctor(doctor);
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                alert2.setTitle("Aviso");
                alert2.setHeaderText(null);
                alert2.setContentText("Doctor dado de alta en Base de Datos");
                alert2.showAndWait();
            } else {
                return;
            }
        }

        // recargar la tabla con el nuevo Doctor
        cargarDoctores();
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
        emaildoc.setStyle("");
        coledocSi.setSelected(false);
        coledocNo.setSelected(false);
        espedoc.setValue(null);
        tablaDoctores.getSelectionModel().clearSelection();
        // recargar la tabla con todos los doctores
        cargarDoctores();
    }

    // cargar Doctores en la tabla
    @FXML 
    private void cargarDoctores() {
        // Creamos el DAO
        DoctorDAOMySQL dao = new DoctorDAOMySQL();
        // Obtenemos los Doctores de la base de datos
        List<Doctor> doctores = dao.cargarDoctores();
        // Los mostramos en la tabla
        tablaDoctores.getItems().setAll(doctores);
        tablaDoctores.refresh();
    }

    @FXML
    public void eliminarDoctor() {
        Doctor seleccionado = tablaDoctores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText("¿Desea Eliminar el Doctor");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DoctorDAOMySQL dao = new DoctorDAOMySQL();
            dao.eliminarDoctor(seleccionado.getIddoc());
            Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
            alert2.setTitle("Aviso");
            alert2.setHeaderText(null);
            alert2.setContentText("Doctor dado de baja en Base de Datos");
            alert2.showAndWait();
        } else {
            return;
        }

        // Recargamos la tabla de Doctores
        limpiarCampos();
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
        DoctorDAOMySQL dao = new DoctorDAOMySQL();
        // Obtenemos los Doctores de la especialidad seleccionada
        List<Doctor> doctoresEncontrados = dao.cargarDoctoresEspec(espedoc.getValue());
        // Los mostramos en la tabla
        tablaDoctores.getItems().setAll(doctoresEncontrados);
    }

}
