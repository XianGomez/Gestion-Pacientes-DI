package com.clase;

import com.clase.modelo.Cita;
import com.clase.modelo.Doctor;
import com.clase.modelo.Paciente;
import com.clase.persistencia.CitaDAOMySQL;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idCita.setEditable(false);
        idDoctor.setEditable(false);
        ToggleGroup estados = new ToggleGroup();
        pendiente.setToggleGroup(estados);
        atendida.setToggleGroup(estados);
        anulada.setToggleGroup(estados);
        pendiente.setSelected(true);

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
        dniPaciente.textProperty().addListener((observable, anterior, nuevo) -> tablaHistorial.getItems().clear());
        tablaHistorial.getSelectionModel().selectedItemProperty().addListener((observable, anterior, cita) -> {
            if (cita != null) {
                mostrarCita(cita);
            }
        });
        dniPaciente.focusedProperty().addListener((observable, anterior, enfocado) -> {
            if (!enfocado && !dniPaciente.getText().trim().isEmpty()) {
                consultarHistorial(false);
            }
        });

        hora.setEditable(true);
        hora.setPromptText("HH:mm");
        for (int minuto = 0; minuto < 24 * 60; minuto += 30) {
            hora.getItems().add(String.format("%02d:%02d", minuto / 60, minuto % 60));
        }
    }

    @FXML
    private void cargarHistorial() {
        consultarHistorial(true);
    }

    void seleccionarPaciente(Paciente paciente) {
        dniPaciente.setText(paciente.getDni());
        idDoctor.clear();
        limpiarFormularioCita();
        consultarHistorial(false);
    }

    void seleccionarDoctor(Doctor doctor) {
        idDoctor.setText(doctor.getIddoc());
        if (!dniPaciente.getText().trim().isEmpty()) {
            consultarHistorial(false);
        }
    }

    private void consultarHistorial(boolean validarDni) {
        String dni = dniPaciente.getText().trim();
        if (dni.isEmpty()) {
            tablaHistorial.getItems().clear();
            if (validarDni) {
                mostrarError("Indica el DNI del paciente", "Escribe el DNI para consultar su historial de citas.");
            }
            return;
        }

        try {
            List<Cita> citas = citaDao.cargarHistorialPaciente(dni);
            tablaHistorial.getItems().setAll(citas);
            if (validarDni && citas.isEmpty()) {
                mostrarInformacion("Historial vacío",
                        "No hay citas guardadas para el DNI " + dni + ".");
            }
        } catch (IllegalStateException e) {
            mostrarError("Error de base de datos", detalle(e));
        }
    }

    @FXML
    private void guardarCita() {
        try {
            String idTexto = idCita.getText().trim();
            String dni = dniPaciente.getText().trim();
            int id = idTexto.isEmpty() ? 0 : Integer.parseInt(idTexto);
            Cita cita = crearCita(id);
            if (id == 0) {
                cita.setIdcita(citaDao.guardarCita(cita));
                idCita.setText(Integer.toString(cita.getIdcita()));
            } else {
                if (citaDao.modificarCita(cita) == 0) {
                    mostrarError("Cita no encontrada", "No existe una cita con ese ID para modificar.");
                    return;
                }
            }
            consultarHistorial(false);
            mostrarInformacion("Cita guardada", id == 0
                    ? "La cita se ha guardado correctamente con el ID " + cita.getIdcita() + "."
                    : "La cita se ha actualizado correctamente.");
        } catch (NumberFormatException e) {
            mostrarError("ID no válido", "El ID de la cita debe ser un número entero.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarError("No se pudo guardar la cita", e instanceof IllegalStateException
                    ? detalle((IllegalStateException) e) : e.getMessage());
        }
    }

    @FXML
    private void eliminarCita() {
        String valor = idCita.getText().trim();
        if (valor.isEmpty()) {
            mostrarError("Indica el ID de la cita", "Busca o escribe el ID de la cita que quieres eliminar.");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            mostrarError("ID no válido", "El ID de la cita debe ser un número entero.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar cita");
        confirmacion.setHeaderText("¿Quieres eliminar la cita " + id + "?");
        if (confirmacion.showAndWait().filter(respuesta -> respuesta == ButtonType.OK).isPresent()) {
            try {
                if (citaDao.eliminarCita(id) == 0) {
                    mostrarError("Cita no encontrada", "No existe una cita con ese ID para eliminar.");
                    return;
                }
                limpiarFormularioCita();
                consultarHistorial(false);
                mostrarInformacion("Cita eliminada", "La cita se ha eliminado correctamente.");
            } catch (IllegalStateException e) {
                mostrarError("No se pudo eliminar la cita", detalle(e));
            }
        }
    }

    @FXML
    private void limpiarCampos() {
        limpiarFormularioCita();
        idDoctor.clear();
        dniPaciente.clear();
        tablaHistorial.getItems().clear();
        tablaHistorial.getSelectionModel().clearSelection();
        pendiente.getToggleGroup().selectToggle(null);
    }

    private void limpiarFormularioCita() {
        idCita.clear();
        motivo.clear();
        fecha.setValue(null);
        hora.setValue(null);
        hora.getEditor().clear();
        diagnostico.clear();
        tratamiento.clear();
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
        String horaCita = hora.getEditor().getText().trim();
        if (horaCita.isEmpty() && hora.getValue() != null) {
            horaCita = hora.getValue();
        }
        if (horaCita.isEmpty()) {
            throw new IllegalArgumentException("Selecciona la hora de la cita.");
        }

        int doctor;
        try {
            doctor = Integer.parseInt(idDoctor.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID del doctor debe ser un número entero.");
        }

        try {
            horaCita = LocalTime.parse(horaCita).toString();
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
                motivo.getText().trim(), diagnostico.getText().trim(), tratamiento.getText().trim(), estado);
    }

    private void mostrarCita(Cita cita) {
        idCita.setText(Integer.toString(cita.getIdcita()));
        idDoctor.setText(Integer.toString(cita.getIdDoctor()));
        dniPaciente.setText(cita.getDniPaciente());
        motivo.setText(cita.getMotivo());
        fecha.setValue(cita.getFecha());
        hora.setValue(formatearHora(cita.getHora()));
        tablaHistorial.getSelectionModel().select(cita);
        diagnostico.setText(cita.getDiagnostico());
        tratamiento.setText(cita.getTratamiento());
        pendiente.setSelected("Pendiente".equalsIgnoreCase(cita.getEstado()));
        atendida.setSelected("Atendida".equalsIgnoreCase(cita.getEstado()));
        anulada.setSelected("Anulada".equalsIgnoreCase(cita.getEstado()));
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

    private String detalle(IllegalStateException e) {
        Throwable causa = e.getCause() == null ? e : e.getCause();
        return causa.getMessage() == null ? causa.toString() : causa.getMessage();
    }
}
