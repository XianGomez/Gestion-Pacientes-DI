package com.clase;

import com.clase.modelo.Paciente;
import com.clase.persistencia.PacienteDAOMySQL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URL;
import java.time.LocalDate;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;



public class PacientesController implements Initializable {

    @FXML
    private TextField dnipac, apelpac, nompac, tlfpac, emailpac, dirpac, tlfopac;
    @FXML
    private DatePicker nacpac;
    @FXML 
    private ComboBox<String> cmbpac, locpac;
    @FXML 
    private Button btnguardarpac, btndelpac;
    @FXML
    private TableView<Paciente> tablaPacientes;
    @FXML
    private TableColumn<Paciente, String> coldnipac, colapelpac, colnompac, colmovilpac, colpropac, colmunipac;

    // referencia (alSeleccionarPaciente) al controlador de citas para poder enviarle el paciente seleccionado
    private Consumer<Paciente> alSeleccionarPaciente = paciente -> { };
    boolean pacienteexiste = false;
    private boolean emailValido = true;

    // Este método se llama automáticamente cuando se carga la vista FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        coldnipac.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colapelpac.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colnompac.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colmovilpac.setCellValueFactory(new PropertyValueFactory<>("movil"));
        colpropac.setCellValueFactory(new PropertyValueFactory<>("provincia"));
        colmunipac.setCellValueFactory(new PropertyValueFactory<>("municipio"));

        dnipac.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { 
                comprobarDni();
            }
        });

        nompac.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && nompac.getText() != null) {
                nompac.setText(formatearNombrePropio(nompac.getText()));
            }
        });

        apelpac.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && apelpac.getText() != null) {
                apelpac.setText(formatearNombrePropio(apelpac.getText()));
            }
        });

        TextField campoTlf = getCampoTelefono();
        if (campoTlf != null) {
            campoTlf.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    comprobarTelefono();
                }
            });
        }

        emailpac.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                validarEmail();
            }
        });

        // carga el paciente seleccionado de la tabla en el formulario
        tablaPacientes.getSelectionModel().selectedItemProperty().addListener((observable, anterior, nuevo) -> {
            if (nuevo != null) {
                alSeleccionarPaciente.accept(nuevo);
                cargarPaciente();
            }
        });

        cargarProvincias();

        cmbpac.setOnAction(e -> cargarMunicipios(cmbpac.getValue()));

        cargarPacientes();
    }

    private boolean validarEmail() {

        String email = emailpac.getText().trim();

        // Vacío = válido (campo opcional)
        if (email.isEmpty()) {
            emailValido = true;
            return true;
        }
        else{
            emailpac.setStyle("-fx-border-color: red;");
            emailpac.clear();
        }

        // Expresión regular para validar el email
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        emailValido = email.matches(regex);

        return emailValido;
    }


    // método para recibir peticiones de citas controller
    void setAlSeleccionarPaciente(Consumer<Paciente> alSeleccionarPaciente) {
        this.alSeleccionarPaciente = alSeleccionarPaciente;
    }

    private void cargarProvincias() {
        try (InputStream entrada = getClass().getResourceAsStream("/com/clase/data/provincias.json")) {
            if (entrada == null) {
                throw new IOException("No se encontró provincias.json");
            }

            JsonObject json = JsonParser.parseReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))
                    .getAsJsonObject();

            JsonArray provincias = json.getAsJsonArray("provincias");

            for (var provincia : provincias) {
                JsonObject p = provincia.getAsJsonObject();
                cmbpac.getItems().add(p.get("nm").getAsString());
            }
        } catch (IOException | RuntimeException e) {
            cmbpac.getItems().clear();
            System.err.println("ERROR al cargar las provincias: " + e.getMessage());
        }
    }

    private void cargarMunicipios(String nombreProvincia) {
        // Eliminamos los municipios que pudiera haber
        // de una selección anterior muy importante sino agrega municipios
        locpac.getItems().clear();
        if (nombreProvincia == null) {
            return;
        }

        try (InputStream entradaProvincias = getClass().getResourceAsStream("/com/clase/data/provincias.json");
             InputStream entradaMunicipios = getClass().getResourceAsStream("/com/clase/data/municipio.json")) {
            if (entradaProvincias == null || entradaMunicipios == null) {
                throw new IOException("No se encontraron los archivos de datos");
            }

            // Obtenemos los dos arrays que necesitamos
            JsonArray provincias = JsonParser.parseReader(
                    new InputStreamReader(entradaProvincias, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("provincias");
            JsonArray municipios = JsonParser.parseReader(
                    new InputStreamReader(entradaMunicipios, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("municipios");

            // Variable donde guardaremos el código de la provincia
            String idProvincia = "";

            // Recorremos las provincias
            for (var provincia : provincias) {
                JsonObject p = provincia.getAsJsonObject();

                // Comprobamos si es la provincia seleccionada
                if (p.get("nm").getAsString().equals(nombreProvincia)) {
                    // Obtenemos su código
                    idProvincia = p.get("id").getAsString();
                    // Ya hemos encontrado la provincia
                    break;
                }
            }

            // Recorremos todos los municipios
            for (var municipio : municipios) {
                JsonObject m = municipio.getAsJsonObject();

                // Comprobamos los dos primeros caracteres del código
                if (m.get("id").getAsString().startsWith(idProvincia)) {
                    // Si pertenecen a la provincia, añadimos su nombre al ComboBox
                    locpac.getItems().add(m.get("nm").getAsString());
                }
            }
        } catch (IOException | RuntimeException e) {
            System.err.println("ERROR al cargar los municipios: " + e.getMessage());
        }
    }

    private TextField getCampoTelefono() {
        return tlfpac != null ? tlfpac : tlfopac;
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
    private void comprobarDni() {
        String dni = dnipac.getText() != null ? dnipac.getText().trim().toUpperCase() : "";
        if (dni.isEmpty())
            return;

        if (validarDniNie(dni)) {
            dnipac.setStyle("");
            dnipac.setText(dni);
        } else {
            dnipac.setStyle("-fx-border-color: red;");
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

    // FUNCIONES CON BASE DE DATOS
    @FXML
    private void guardarPaciente() {
        if (nacpac.getValue() == null) {
            System.err.println("No se puede guardar: debes introducir la fecha de nacimiento");
            return;
        }

        String dni = dnipac.getText().toUpperCase();
        String apellidos = apelpac.getText();
        String nombre = nompac.getText();
        LocalDate fechaNacimiento = nacpac.getValue();
        String movil = tlfopac.getText();
        String email = emailpac.getText();
        String direccion = dirpac.getText();
        String provincia = cmbpac.getValue();
        String municipio = locpac.getValue(); 

        if (dni.isBlank() || apellidos.isBlank() || nombre.isBlank() || movil.isBlank()
                || direccion.isBlank() || provincia == null || municipio == null) {
            System.err.println("No se puede guardar: completa todos los campos obligatorios");
            return;
        }

        // Creamos el objeto Paciente
        Paciente paciente = new Paciente(dni, apellidos, nombre, fechaNacimiento, movil, email, direccion, provincia, municipio);

        if (pacienteexiste) {
            Alert alert3 = new Alert(Alert.AlertType.CONFIRMATION);
            alert3.setTitle("Aviso");
            alert3.setHeaderText(null);
            alert3.setContentText("¿Desea Modificar Datos Paciente?");
            Optional<ButtonType> result = alert3.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Si el paciente ya existe, lo actualizamos en la base de datos
                PacienteDAOMySQL dao = new PacienteDAOMySQL();
                dao.modificarPaciente(paciente.getDni(), paciente);
                Alert alert4 = new Alert(Alert.AlertType.INFORMATION);
                alert4.setTitle("Información");
                alert4.setHeaderText(null);
                alert4.setContentText("Paciente Modificado");
                alert4.showAndWait();
            } else {
                return;
            }
            pacienteexiste = false; // importante

        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("¿Desea Grabar Nuevo Paciente?");
            Optional<ButtonType> result2 = alert.showAndWait();
            if (result2.isPresent() && result2.get() == ButtonType.OK) {
                // Si el paciente no existe, lo guardamos en la base de datos
                PacienteDAOMySQL dao = new PacienteDAOMySQL();
                dao.guardarPaciente(paciente);
                Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                alert2.setTitle("Información");
                alert2.setHeaderText(null);
                alert2.setContentText("Paciente Guardado");
                alert2.showAndWait();
            } else {
                return;
            }
        }

        // recargar la tabla con el nuevo paciente
        cargarPacientes();
    }

    public void cargarPaciente() {
        Paciente pacienteselect = tablaPacientes.getSelectionModel().getSelectedItem(); // carga los datos de la fila seleccionada
        if (pacienteselect == null) {
            return;
        }

        pacienteexiste = true;

        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        Paciente paciente = dao.buscarPaciente(pacienteselect.getDni());
        if (paciente == null) {
            return;
        }

        dnipac.setText(paciente.getDni());
        apelpac.setText(paciente.getApellidos());
        nompac.setText(paciente.getNombre());
        getCampoTelefono().setText(paciente.getMovil());
        emailpac.setText(paciente.getEmail());
        dirpac.setText(paciente.getDireccion());
        nacpac.setValue(paciente.getNacimiento());
        cmbpac.setValue(paciente.getProvincia());
        locpac.setValue(paciente.getMunicipio());
    }

    @FXML
    private void limpiarCampos() {
        pacienteexiste = false;
        dnipac.clear();
        apelpac.clear();
        nompac.clear();
        nacpac.setValue(null);
        tlfopac.clear();
        emailpac.clear();
        dirpac.clear();
        cmbpac.setValue(null);
        locpac.getItems().clear();
        dnipac.setStyle("");
        tlfopac.setStyle("");
        emailpac.setStyle("");
        tablaPacientes.getSelectionModel().clearSelection();
    }

    // cargar pacientes en la tabla
    @FXML 
    private void cargarPacientes() {
        // Creamos el DAO
        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        // Obtenemos los pacientes de la base de datos
        List<Paciente> pacientes = dao.cargarPacientes();
        // Los mostramos en la tabla
        tablaPacientes.getItems().setAll(pacientes);
    }

    @FXML 
    public void eliminarPaciente() {
        Paciente seleccionado = tablaPacientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText("¿Desea Eliminar Datos Paciente?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            PacienteDAOMySQL dao = new PacienteDAOMySQL();
            dao.eliminarPaciente(seleccionado.getDni());
            Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
            alert2.setTitle("Aviso");
            alert2.setHeaderText(null);
            alert2.setContentText("Paciente Eliminado de Base de Datos");
            alert2.showAndWait();
        } else {
            return;
        }

        // Recargamos la tabla de pacientes
        limpiarCampos();
        cargarPacientes();
    }

    @FXML
    private void buscaPacDni() {

        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        Paciente paciente = dao.buscaPacDni(dnipac.getText());
        if (paciente == null) {
            Alert alert2 = new Alert(Alert.AlertType.WARNING);
            alert2.setTitle("Aviso");
            alert2.setHeaderText(null);
            alert2.setContentText("Paciente no existe en BBDD");
            alert2.showAndWait();
            return;
        } else {
            pacienteexiste = true;
            // Enviamos el paciente al controlador de citas
            alSeleccionarPaciente.accept(paciente);
        }

        dnipac.setText(paciente.getDni());
        apelpac.setText(paciente.getApellidos());
        nompac.setText(paciente.getNombre());
        tlfopac.setText(paciente.getMovil());
        emailpac.setText(paciente.getEmail());
        dirpac.setText(paciente.getDireccion());
        nacpac.setValue(paciente.getNacimiento());
        cmbpac.setValue(paciente.getProvincia());
        locpac.setValue(paciente.getMunicipio());

    }

}
