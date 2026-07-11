package services;

import java.awt.Color;
import java.net.URL;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import models.BoletinAdmin;
import models.Incidente;

public class FormularioPDFRenderer {

    private final Color AZUL = new Color(78, 132, 204);

    public void renderBoletinEmpleado(Document documento, PdfWriter writer, Incidente i) throws Exception {
        dibujarFormularioBase(documento, writer, "BOLETÍN DE OCURRENCIA");

        PdfContentByte cb = writer.getDirectContent();
        Font texto = FontFactory.getFont(FontFactory.HELVETICA, 9);

        escribirCampo(cb, 160, 708, 280, 724, formatearFecha(i.getFecha()), texto);
        escribirCampo(cb, 440, 708, 560, 724, formatearFecha(i.getFecha()), texto);

        escribirCampo(cb, 105, 668, 540, 684, valor(i.getSector()), texto);
        escribirCampo(cb, 105, 625, 300, 641, valor(i.getTitulo()), texto);
        escribirCampo(cb, 430, 585, 545, 640, valor(i.getDescripcion()), texto);

        escribirCampo(cb, 160, 512, 300, 528, valor(i.getNombreEmpleado()), texto);

        escribirCampo(cb, 105, 405, 300, 421, valor(i.getSector()), texto);

        escribirCampo(cb, 500, 40, 560, 55, "Prioridad: " + valor(String.valueOf(i.getPrioridad())), texto);
    }

    public void renderBoletinAdministrador(Document documento, PdfWriter writer, BoletinAdmin b, int numero) throws Exception {
        dibujarFormularioBase(documento, writer, "BOLETÍN DE INVESTIGACIÓN N° " + numero);

        PdfContentByte cb = writer.getDirectContent();
        Font texto = FontFactory.getFont(FontFactory.HELVETICA, 9);

        escribirCampo(cb, 160, 708, 280, 724, valor(b.getFechaRegistro()), texto);
        escribirCampo(cb, 410, 708, 540, 724, valor(b.getFechaEmision()), texto);

        escribirCampo(cb, 105, 668, 540, 684, valor(b.getLugar()), texto);
        escribirCampo(cb, 105, 625, 300, 641, valor(b.getTitulo()), texto);
        escribirCampo(cb, 430, 585, 545, 640, valor(b.getDescripcion()), texto);

        escribirCampo(cb, 160, 512, 300, 528, valor(b.getNombreApellido()), texto);
        escribirCampo(cb, 395, 512, 540, 528, valor(b.getCargo()), texto);
        escribirCampo(cb, 160, 477, 300, 493, valor(b.getMatricula()), texto);
        escribirCampo(cb, 395, 477, 540, 493, valor(b.getDni()), texto);

        escribirCampo(cb, 105, 405, 300, 421, valor(b.getArea()), texto);
        escribirCampo(cb, 430, 405, 540, 421, valor(b.getSuperiorInmediato()), texto);

        escribirCampo(cb, 40, 215, 550, 350, valor(b.getHistorial()), texto);

        escribirCampo(cb, 500, 40, 560, 55, "Prioridad: " + valor(b.getPrioridad()), texto);
    }

    private void dibujarFormularioBase(Document documento, PdfWriter writer, String tituloFormulario) throws Exception {
        PdfContentByte cb = writer.getDirectContent();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
        Font texto = FontFactory.getFont(FontFactory.HELVETICA, 9);
        Font textoBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
        Font blanco = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);

     // Marco principal
        cb.rectangle(20, 755, 555, 65);
        cb.stroke();

        // Logo
        cb.rectangle(20, 755, 90, 65);
        cb.stroke();

        // Título
        cb.rectangle(110, 755, 370, 65);
        cb.stroke();

        // Código
        cb.rectangle(480, 755, 95, 65);
        cb.stroke();

        URL logoUrl = getClass().getResource("/resources/images/stellantis.jpg");

        if (logoUrl != null) {
            Image logo = Image.getInstance(logoUrl);
            logo.scaleToFit(70, 42);
            logo.setAbsolutePosition(31, 767);
            documento.add(logo);
        } else {
            textoCentrado(cb, 90, 790, "STELLANTIS", textoBold);
        }

        textoCentrado(cb, 295, 793, tituloFormulario, titulo);

        barraAzul(cb, 480, 792, 95, 28, "CÓDIGO", blanco);
        textoCentrado(cb, 528, 773, "R-RRHH-SP-004", textoBold);

        campo(cb, 40, 700, 245, 25, "Fecha/Hora Registro:", textoBold);
        campo(cb, 330, 700, 225, 25, "Fecha/Hora Emisión:", textoBold);

        campo(cb, 40, 660, 515, 25, "Lugar:", textoBold);

        campo(cb, 40, 617, 290, 25, "Título:", textoBold);

        texto(cb, 355, 635, "Descripción:", textoBold);
        cb.rectangle(425, 585, 130, 60);
        cb.stroke();

        barraAzul(cb, 30, 545, 535, 22, "INVOLUCRADOS:", blanco);

        campo(cb, 40, 505, 245, 25, "Nombre y Apellido:", textoBold);
        campo(cb, 330, 505, 225, 25, "Cargo:", textoBold);
        campo(cb, 40, 470, 245, 25, "Matrícula:", textoBold);
        campo(cb, 330, 470, 225, 25, "DNI:", textoBold);

        barraAzul(cb, 30, 435, 535, 22, "SECTOR:", blanco);

        campo(cb, 40, 397, 245, 25, "Área:", textoBold);
        campo(cb, 330, 397, 225, 25, "Superior inmediato:", textoBold);

        barraAzul(cb, 30, 360, 535, 22, "HISTORIAL:", blanco);

        cb.rectangle(30, 200, 535, 160);
        cb.stroke();

        barraAzul(cb, 30, 160, 535, 22, "IMÁGENES ADJUNTAS:", blanco);

        cb.rectangle(30, 60, 535, 100);
        cb.stroke();
    }

    private void campo(PdfContentByte cb, float x, float y, float w, float h, String label, Font fuente) {
        cb.rectangle(x, y, w, h);
        cb.stroke();
        texto(cb, x + 5, y + 8, label, fuente);
    }

    private void barraAzul(PdfContentByte cb, float x, float y, float w, float h, String texto, Font fuente) {
        cb.setColorFill(AZUL);
        cb.rectangle(x, y, w, h);
        cb.fill();
        cb.setColorFill(Color.BLACK);

        ColumnText.showTextAligned(
                cb,
                Element.ALIGN_LEFT,
                new Phrase(texto, fuente),
                x + 6,
                y + 7,
                0
        );
    }

    private void texto(PdfContentByte cb, float x, float y, String texto, Font fuente) {
        ColumnText.showTextAligned(
                cb,
                Element.ALIGN_LEFT,
                new Phrase(valor(texto), fuente),
                x,
                y,
                0
        );
    }

    private void textoCentrado(PdfContentByte cb, float x, float y, String texto, Font fuente) {
        ColumnText.showTextAligned(
                cb,
                Element.ALIGN_CENTER,
                new Phrase(valor(texto), fuente),
                x,
                y,
                0
        );
    }

    private void escribirCampo(PdfContentByte cb, float x1, float y1, float x2, float y2, String texto, Font fuente) {
        ColumnText ct = new ColumnText(cb);
        ct.setSimpleColumn(new Phrase(valor(texto), fuente), x1, y1, x2, y2, 12, Element.ALIGN_LEFT);
        try {
            ct.go();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String valor(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }
    
    private String formatearFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return "";
        }

        try {
            LocalDateTime f = LocalDateTime.parse(fecha);
            return f.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        } catch (Exception e) {
            return fecha;
        }
    }
    
}