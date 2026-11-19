package com.clase;

import com.clase.modelo.Cita;
import com.clase.modelo.Doctor;
import com.clase.modelo.Paciente;
import com.clase.persistencia.CitaDAOMySQL;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyStringWrapper;

public class CitasController implements Initializable {
    @FXML private TextField idCita;
    @FXML private TextField idDoctor;
    @FXML private TextField dniPaciente;
    @FXML private TextField motivo;
    @FXML private DatePicker fecha;
    @FXML private javafx.scene.control.ComboBox<String> hora;
    @FXML private RadioButton pendiente;
    @FXML private RadioButton atendida;
    @FXML private RadioButton anulada;
    @FXML private TextArea diagnostico;
    @FXML private TextArea tratamiento;
    @FXML private TableView<Cita> tablaHistorial;
    @FXML private TableColumn<Cita, Integer> colIdHistorial;
    @FXML private TableColumn<Cita, String> colFechaHistorial;
    @FXML private TableColumn<Cita, String> colHoraHistorial;
    @FXML private TableColumn<Cita, String> colDoctorHistorial;
    @FXML private TableColumn<Cita, String> colEspecialidadHistorial;
    @FXML private TableColumn<Cita, String> colMotivoHistorial;
    @FXML private TableColumn<Cita, String> colDiagnosticoHistorial;
    @FXML private TableColumn<Cita, String> colTratamientoHistorial;
    @FXML private TableColumn<Cita, String> colEstadoHistorial;

    private final CitaDAOMySQL citaDao = new CitaDAOMySQL();

    boolean citaexiste = false;
    private Paciente pacienteSeleccionado;
    private Doctor doctorSeleccionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCita.setEditable(false);
        idDoctor.setEditable(false);
        dniPaciente.setEditable(false);
        ToggleGroup estados = new ToggleGroup();
        pendiente.setToggleGroup(estados);
        atendida.setToggleGroup(estados);
        anulada.setToggleGroup(estados);
        pendiente.setSelected(true);

        // Configuramos las columnas de la tabla para que muestren los datos de las citas
        colIdHistorial.setCellValueFactory(new PropertyValueFactory<>("idcita"));
        colFechaHistorial.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getFecha() == null ? "" : cell.getValue().getFecha().toString()));
        colHoraHistorial.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                formatearHora(cell.getValue().getHora())));
        colDoctorHistorial.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        colEspecialidadHistorial.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
        colMotivoHistorial.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colDiagnosticoHistorial.setCellValueFactory(new PropertyValueFactory<>("diagnostico"));
        colTratamientoHistorial.setCellValueFactory(new PropertyValueFactory<>("tratamiento"));
        colEstadoHistorial.setCellValueFactory(new PropertyValueFactory<>("estado"));
        tablaHistorial.setPlaceholder(new Label("No hay citas registradas para este paciente."));

        // Carga una cita al seleccionarla en la tabla
        // en el formulario para poder modificarla o eliminarla
        tablaHistorial.getSelectionModel().selectedItemProperty().addListener((observable, anterior, cita) -> {
            if (cita != null) {
                mostrarCita(cita);
            }
        });

        // Inicialización de la vista de citas
        hora.setPromptText("HH:mm");
        cargarHorario();
        if (dniPaciente.getText() != null && !dniPaciente.getText().isEmpty()) {
            cargarHistorial();
        }
    }

    // el horario sale del fichero horario.json
    private void cargarHorario() {
        try (InputStream entrada = getClass().getResourceAsStream("/com/clase/data/horario.json")) {
            if (entrada == null) {
                throw new IOException("No se encontró horario.json");
            }

            // Leemos el JSON y lo convertimos en un objeto JsonObject
            JsonObject json = JsonParser.parseReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))
                    .getAsJsonObject();

            // Obtenemos el array "horario" del JSON
            JsonArray horas = json.getAsJsonArray("horario");

            // Recorremos todas las horas y las añadimos al ComboBox
            for (var h : horas) {
                hora.getItems().add(h.getAsString());
            }
        } catch (IOException | RuntimeException e) {
            hora.getItems().clear();
            System.err.println("ERROR al cargar el horario: " + e.getMessage());
        }
    }

    // cargar las citas del paciente en la tabla
    @FXML
    private void cargarHistorial() {
        String dni = dniPaciente.getText().trim();
        if (dni.isEmpty()) {
            tablaHistorial.getItems().clear();
            return;
        }

        List<Cita> citas = citaDao.cargarCitaspaciente(dni);
        tablaHistorial.getItems().setAll(citas);
        tablaHistorial.getSelectionModel().clearSelection();
        tablaHistorial.refresh();
    }

    // Recibe el paciente seleccionado
    void seleccionarPaciente(Paciente paciente) {
        pacienteSeleccionado = paciente;
        dniPaciente.setText(paciente.getDni());
        limpiarDatosCita();
        cargarHistorial();
    }

    // Recibe el doctor seleccionado
    void seleccionarDoctor(Doctor doctor) {
        doctorSeleccionado = doctor;
        idDoctor.setText(doctor.getIddoc());
    }

    void mostrarDatosSeleccionados() {
        if (pacienteSeleccionado != null) {
            dniPaciente.setText(pacienteSeleccionado.getDni());
            cargarHistorial();
        }
        if (doctorSeleccionado != null) {
            idDoctor.setText(doctorSeleccionado.getIddoc());
        }
    }

    //FUNCION CRUD DE CITAS

    @FXML
    private void guardarCita() {
        try {
            String idTexto = idCita.getText().trim();
            int id = citaexiste && !idTexto.isEmpty() ? Integer.parseInt(idTexto) : 0;
            Cita cita = crearCita(id);

            if (citaexiste) {
                // Si la cita ya existe, la actualizamos en la base de datos
                Alert alert3 = new Alert(Alert.AlertType.CONFIRMATION);
                alert3.setTitle("Aviso");
                alert3.setHeaderText(null);
                alert3.setContentText("¿Desea Modificar datos del Cita?");
                Optional<ButtonType> result2 = alert3.showAndWait();
                if (result2.isPresent() && result2.get() == ButtonType.OK) {
                    citaDao.modificarCita(cita);
                    citaexiste = false; // importante sino siempre graba
                    Alert alert4 = new Alert(Alert.AlertType.INFORMATION);
                    alert4.setTitle("Aviso");
                    alert4.setHeaderText(null);
                    alert4.setContentText("Cita modificada en Base de Datos");
                    alert4.showAndWait();
                } else {
                    return;
                }
            } else {
                // Si la Cita no existe, la guardamos en la base de datos
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Aviso");
                alert.setHeaderText(null);
                alert.setContentText("¿Desea Grabar el Cita?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    citaDao.guardarCita(cita);
                    Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
                    alert2.setTitle("Aviso");
                    alert2.setHeaderText(null);
                    alert2.setContentText("Cita Guardada en Base de Datos");
                    alert2.showAndWait();
                } else {
                    return;
                }
            }
            limpiarDatosCita();
            cargarHistorial();
        } catch (NumberFormatException e) {
            mostrarError("ID no válido", "El ID de la cita debe ser un número entero.");
        } catch (IllegalArgumentException e) {
            mostrarError("No se pudo guardar la cita", e.getMessage());
        }
    }

    @FXML
    private void eliminarCita() {
        Cita citaSeleccionada = tablaHistorial.getSelectionModel().getSelectedItem();
        if (citaSeleccionada != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmación");
            alert.setHeaderText(null);
            alert.setContentText("¿Está seguro de que desea eliminar la cita seleccionada?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                citaDao.eliminaCita(citaSeleccionada.getIdcita());
                limpiarDatosCita();
                cargarHistorial();
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Información");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("Cita eliminada correctamente.");
                infoAlert.showAndWait();
            }
        } else {
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Información");
            infoAlert.setHeaderText(null);
            infoAlert.setContentText("Por favor, seleccione una cita para eliminar.");
            infoAlert.showAndWait();
        }
    }

    @FXML
    private void limpiarCampos() {
        pacienteSeleccionado = null;
        doctorSeleccionado = null;
        idDoctor.clear();
        dniPaciente.clear();
        limpiarDatosCita();
        tablaHistorial.getItems().clear();
    }

    private void limpiarDatosCita() {
        citaexiste = false;
        idCita.clear();
        motivo.clear();
        fecha.setValue(null);
        hora.setValue(null);
        diagnostico.clear();
        tratamiento.clear();
        tablaHistorial.getSelectionModel().clearSelection();
        pendiente.setSelected(true);
    }

    private Cita crearCita(int id) {
        if (idDoctor.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Selecciona un doctor.");
        }
        if (dniPaciente.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Selecciona un paciente.");
        }
        if (fecha.getValue() == null) {
            throw new IllegalArgumentException("Selecciona la fecha de la cita.");
        }
        String horaCita = hora.getValue();
        if (horaCita == null || horaCita.trim().isEmpty()) {
            throw new IllegalArgumentException("Selecciona la hora de la cita.");
        }

        int doctor;
        try {
            doctor = Integer.parseInt(idDoctor.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID del doctor debe ser un número entero.");
        }

        try {
            horaCita = LocalTime.parse(horaCita.trim()).toString();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Indica una hora válida en formato HH:mm.");
        }

        String estado = pendiente.isSelected() ? "Pendiente"
                : atendida.isSelected() ? "Atendida"
                : anulada.isSelected() ? "Anulada" : null;
        if (estado == null) {
            throw new IllegalArgumentException("Selecciona el estado de la cita.");
        }
        return new Cita(id, dniPaciente.getText().trim(), doctor, fecha.getValue(), horaCita,
                textoSeguro(motivo.getText()), textoSeguro(diagnostico.getText()), textoSeguro(tratamiento.getText()), estado);
    }

    // carga la cita seleccionada en el formulario (se vuelve a consultar en la base de datos)
    private void mostrarCita(Cita cita) {
        cita = citaDao.buscarCita(cita.getIdcita());
        if (cita == null) {
            return;
        }

        idCita.setText(Integer.toString(cita.getIdcita()));
        idDoctor.setText(Integer.toString(cita.getIdDoctor()));
        dniPaciente.setText(cita.getDniPaciente());
        motivo.setText(cita.getMotivo());
        fecha.setValue(cita.getFecha());
        hora.setValue(formatearHora(cita.getHora()));
        diagnostico.setText(cita.getDiagnostico());
        tratamiento.setText(cita.getTratamiento());

        // Establecer el estado de la cita en los RadioButton
        pendiente.setSelected("Pendiente".equalsIgnoreCase(cita.getEstado()));
        atendida.setSelected("Atendida".equalsIgnoreCase(cita.getEstado()));
        anulada.setSelected("Anulada".equalsIgnoreCase(cita.getEstado()));

        citaexiste = true; // Marcamos que la cita existe para futuras modificaciones
    }

    // un campo de texto con valor null (p. ej. diagnóstico sin rellenar en la BBDD) se trata como vacío
    private String textoSeguro(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String formatearHora(String horaTexto) {
        return horaTexto == null || horaTexto.isEmpty() ? ""
                : LocalTime.parse(horaTexto).format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Gestión de citas");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Gestión de citas");
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
