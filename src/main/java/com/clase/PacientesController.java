package com.clase;

import com.clase.modelo.Paciente;
import com.clase.persistencia.PacienteDAOMySQL;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URL;
import java.time.LocalDate;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;


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

    private Consumer<Paciente> alSeleccionarPaciente = paciente -> { };
    boolean pacienteexiste = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cargarProvincias();
        cmbpac.valueProperty().addListener((observable, oldValue, newValue) -> cargarMunicipios(newValue));

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

        coldnipac.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colapelpac.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colnompac.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colmovilpac.setCellValueFactory(new PropertyValueFactory<>("movil"));
        colpropac.setCellValueFactory(new PropertyValueFactory<>("provincia"));
        colmunipac.setCellValueFactory(new PropertyValueFactory<>("municipio"));
        cargarPacientes();

        tablaPacientes.getSelectionModel().selectedItemProperty().addListener((observable, anterior, nuevo) -> {
            if (nuevo != null) {
                cargarPaciente();
                alSeleccionarPaciente.accept(nuevo);
            }
        });
    }

    void setAlSeleccionarPaciente(Consumer<Paciente> alSeleccionarPaciente) {
        this.alSeleccionarPaciente = alSeleccionarPaciente;
    }

    private void cargarProvincias() {
        try (InputStream entrada = getClass().getResourceAsStream("/com/clase/data/provincias.json")) {
            if (entrada == null) {
                throw new IOException("No se encontró provincias.json");
            }

            JsonObject datos = JsonParser.parseReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            Provincia[] provincias = new Gson().fromJson(datos.getAsJsonArray("provincias"), Provincia[].class);

            cmbpac.getItems().setAll(Arrays.stream(provincias)
                    .map(Provincia::getNm)
                    .collect(Collectors.toList()));
        } catch (IOException | RuntimeException e) {
            cmbpac.getItems().clear();
            System.err.println("ERROR al cargar las provincias: " + e.getMessage());
        }
    }

    private void cargarMunicipios(String nombreProvincia) {
        locpac.getItems().clear();
        if (nombreProvincia == null) {
            return;
        }

        try (InputStream entradaProvincias = getClass().getResourceAsStream("/com/clase/data/provincias.json");
             InputStream entradaMunicipios = getClass().getResourceAsStream("/com/clase/data/municipio.json")) {
            if (entradaProvincias == null || entradaMunicipios == null) {
                throw new IOException("No se encontraron los archivos de datos");
            }

            Gson gson = new Gson();
            JsonObject datosProvincias = JsonParser.parseReader(
                    new InputStreamReader(entradaProvincias, StandardCharsets.UTF_8)).getAsJsonObject();
            Provincia provincia = Arrays.stream(gson.fromJson(
                            datosProvincias.getAsJsonArray("provincias"), Provincia[].class))
                    .filter(elemento -> elemento.getNm().equals(nombreProvincia))
                    .findFirst()
                    .orElse(null);

            if (provincia == null) {
                return;
            }

            JsonObject datosMunicipios = JsonParser.parseReader(
                    new InputStreamReader(entradaMunicipios, StandardCharsets.UTF_8)).getAsJsonObject();
                Municipio[] municipios = gson.fromJson(datosMunicipios.getAsJsonArray("municipios"), Municipio[].class);
            List<String> nombresMunicipios = Arrays.stream(municipios)
                    .filter(municipio -> municipio.getCodigoProvincia().equals(String.format("%02d", provincia.getId())))
                    .map(Municipio::getNm)
                    .sorted()
                    .collect(Collectors.toList());
            locpac.getItems().setAll(nombresMunicipios);
        } catch (IOException | RuntimeException e) {
            System.err.println("ERROR al cargar los municipios: " + e.getMessage());
        }
    }

    private TextField getCampoTelefono() {
        return tlfpac != null ? tlfpac : tlfopac;
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
    private void comprobarDni() {
        String dni = dnipac.getText() != null ? dnipac.getText().trim().toUpperCase() : "";
        
        if (dni.isEmpty()) {
            dnipac.setStyle("");
            return;
        }
        
        if (validarDniNie(dni)) {
            dnipac.setStyle("-fx-border-color: green; -fx-border-width: 1.5px;");
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
        // Creamos el objeto Paciente 
        if (dni.isBlank() || apellidos.isBlank() || nombre.isBlank() || movil.isBlank()
                || direccion.isBlank() || provincia == null || municipio == null) {
            System.err.println("No se puede guardar: completa todos los campos obligatorios");
            return;
        }

        Paciente paciente = new Paciente(dni, apellidos, nombre, fechaNacimiento, movil, email, direccion, provincia, municipio);

        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        if (pacienteexiste) {
            dao.modificarPaciente(paciente.getDni(), paciente);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText("Sanidad Teis");
            alert.setContentText("Paciente Modificado Correctamente");
            alert.showAndWait();
            pacienteexiste = false;
            buscaPacDni();
        } else {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar alta");
            confirmacion.setHeaderText(null);
            confirmacion.setContentText("¿Confirmas que quieres crear este paciente?");
            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                return;
            }

            dao.guardarPaciente(paciente);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText("Sanidad Teis");
            alert.setContentText("Paciente Guardado Correctamente");
            alert.showAndWait();
        }

        cargarPacientes();
    }

    public void cargarPaciente() {
        Paciente paciente = tablaPacientes.getSelectionModel().getSelectedItem();
        if (paciente == null) {
            return;
        }

        pacienteexiste = true;
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
        tablaPacientes.getSelectionModel().clearSelection();
    }

    @FXML 
    private void cargarPacientes() {
        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        List<Paciente> pacientes = dao.cargarPacientes();
        tablaPacientes.getItems().setAll(pacientes);
    }

    @FXML 
    public void eliminarPaciente() {
        Paciente seleccionado = tablaPacientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            return;
        }

        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        try {
            
            
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Aviso");
            alert.setHeaderText(null);
            alert.setContentText("Quieres eliminar al paciente?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                dao.eliminarPaciente(seleccionado.getDni());
                tablaPacientes.getItems().remove(seleccionado);
                limpiarCampos();
                
            }
            
        } catch (IllegalStateException e) {
            System.err.println("ERROR al eliminar el paciente: " + e.getMessage());
        }
    }

    @FXML
    private void buscaPacDni() {

        PacienteDAOMySQL dao = new PacienteDAOMySQL();
        Paciente paciente = dao.buscaPacDni(dnipac.getText());
        if (paciente == null) {
            System.err.println("No se encontró un paciente con ese DNI");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Búsqueda de paciente");
            alert.setHeaderText(null);
            alert.setContentText("No se encontró un paciente con ese DNI en la BBD.");
            alert.showAndWait();
            return;
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
