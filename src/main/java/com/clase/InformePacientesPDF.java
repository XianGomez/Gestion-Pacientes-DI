package com.clase;

import com.clase.modelo.Paciente;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

final class InformePacientesPDF {
    private static final String[] COLUMNAS = {
        "DNI", "Apellidos", "Nombre", "Nacimiento", "Móvil",
        "Correo", "Dirección", "Provincia", "Municipio"
    };

    private InformePacientesPDF() {
    }

    static void exportar(File archivo, List<Paciente> pacientes) throws IOException {
        Document documento = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);
        try (FileOutputStream salida = new FileOutputStream(archivo)) {
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Paragraph titulo = new Paragraph("Listado de pacientes", new Font(Font.HELVETICA, 16, Font.BOLD));
            titulo.setAlignment(Element.ALIGN_CENTER);
            documento.add(titulo);
            Paragraph fecha = new Paragraph("Generado el " + LocalDate.now()
                    + " - Total de pacientes: " + pacientes.size(),
                    new Font(Font.HELVETICA, 9));
            fecha.setAlignment(Element.ALIGN_CENTER);
            documento.add(fecha);
            documento.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(COLUMNAS.length);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[] {1.0f, 1.5f, 1.2f, 1.2f, 1.1f, 1.8f, 2.2f, 1.3f, 1.3f});
            tabla.setHeaderRows(1);
            Font fuenteCabecera = new Font(Font.HELVETICA, 8, Font.BOLD);
            Font fuenteCelda = new Font(Font.HELVETICA, 8);

            for (String columna : COLUMNAS) {
                PdfPCell celda = new PdfPCell(new Phrase(columna, fuenteCabecera));
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                celda.setPadding(5);
                tabla.addCell(celda);
            }

            for (Paciente paciente : pacientes) {
                agregarCelda(tabla, paciente.getDni(), fuenteCelda);
                agregarCelda(tabla, paciente.getApellidos(), fuenteCelda);
                agregarCelda(tabla, paciente.getNombre(), fuenteCelda);
                agregarCelda(tabla, paciente.getNacimiento() == null
                        ? "" : paciente.getNacimiento().toString(), fuenteCelda);
                agregarCelda(tabla, paciente.getMovil(), fuenteCelda);
                agregarCelda(tabla, paciente.getEmail(), fuenteCelda);
                agregarCelda(tabla, paciente.getDireccion(), fuenteCelda);
                agregarCelda(tabla, paciente.getProvincia(), fuenteCelda);
                agregarCelda(tabla, paciente.getMunicipio(), fuenteCelda);
            }

            if (pacientes.isEmpty()) {
                PdfPCell vacio = new PdfPCell(new Phrase("No hay pacientes registrados.", fuenteCelda));
                vacio.setColspan(COLUMNAS.length);
                vacio.setHorizontalAlignment(Element.ALIGN_CENTER);
                vacio.setPadding(8);
                tabla.addCell(vacio);
            }
            documento.add(tabla);
            documento.close();
        } catch (DocumentException e) {
            throw new IOException("No se pudo crear el documento PDF.", e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
    }

    private static void agregarCelda(PdfPTable tabla, String valor, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(valor == null ? "" : valor, fuente));
        celda.setPadding(4);
        tabla.addCell(celda);
    }
}
