package services;

import java.awt.Color;
import java.net.URL;
import java.util.Comparator;
import java.util.List;

import api.FirmaExpedienteApiResponse;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import models.BoletinAdmin;
import models.Incidente;

public class FormularioPDFRenderer {

    private final Color AZUL = new Color(78, 132, 204);

    public void renderBoletinEmpleado(Document documento, PdfWriter writer, Incidente i) throws Exception {
        dibujarFormularioBase(documento, writer, "BOLETÍN DE OCURRENCIA");

        PdfContentByte cb = writer.getDirectContent();
        Font texto = FontFactory.getFont(FontFactory.HELVETICA, 9);

        escribirCampo(cb, 160, 708, 280, 724, formatearFecha(i.getFechaRegistro()), texto);
        escribirCampo(cb, 440, 708, 560, 724, formatearFecha(i.getFechaEmision()), texto);

        escribirCampo(cb, 105, 668, 540, 684, valor(i.getLugar()), texto);
        escribirCampo(cb, 105, 625, 300, 641, valor(i.getTitulo()), texto);
        boolean descripcionContinua = escribirCampo(
				cb, 430, 585, 545, 640, valor(i.getDescripcion()), texto
		);

        escribirCampo(cb, 160, 512, 300, 528, valor(i.getNombreApellido()), texto);
        escribirCampo(cb, 395, 512, 540, 528, valor(i.getCargo()), texto);
        escribirCampo(cb, 160, 477, 300, 493, valor(i.getMatricula()), texto);
        escribirCampo(cb, 395, 477, 540, 493, valor(i.getDni()), texto);

        escribirCampo(cb, 105, 405, 300, 421, valor(i.getArea()), texto);
        escribirCampo(cb, 430, 405, 540, 421, valor(i.getSuperiorInmediato()), texto);
        ColumnText historialPendiente = escribirHistorialInicial(
                cb, valor(i.getHistorial()), texto
        );

        escribirCampo(cb, 455, 40, 560, 55, "Prioridad: " + valor(String.valueOf(i.getPrioridad())), texto);

        continuarHistorial(documento, writer, historialPendiente);

		agregarContinuacionSiHaceFalta(
				documento,
				"BOLETÍN ORIGINAL DEL EMPLEADO",
				descripcionContinua ? valor(i.getDescripcion()) : null
		);
    }

    public void renderBoletinAdministrador(Document documento, PdfWriter writer, BoletinAdmin b, int numero) throws Exception {
        dibujarFormularioBase(documento, writer, "BOLETÍN DE INVESTIGACIÓN N° " + numero);

        PdfContentByte cb = writer.getDirectContent();
        Font texto = FontFactory.getFont(FontFactory.HELVETICA, 9);

        escribirCampo(cb, 160, 708, 280, 724, formatearFecha(b.getFechaRegistro()), texto);
        escribirCampo(cb, 410, 708, 540, 724, formatearFecha(b.getFechaEmision()), texto);

        escribirCampo(cb, 105, 668, 540, 684, valor(b.getLugar()), texto);
        escribirCampo(cb, 105, 625, 300, 641, valor(b.getTitulo()), texto);
        boolean descripcionContinua = escribirCampo(
				cb, 430, 585, 545, 640, valor(b.getDescripcion()), texto
		);

        escribirCampo(cb, 160, 512, 300, 528, valor(b.getNombreApellido()), texto);
        escribirCampo(cb, 395, 512, 540, 528, valor(b.getCargo()), texto);
        escribirCampo(cb, 160, 477, 300, 493, valor(b.getMatricula()), texto);
        escribirCampo(cb, 395, 477, 540, 493, valor(b.getDni()), texto);

        escribirCampo(cb, 105, 405, 300, 421, valor(b.getArea()), texto);
        escribirCampo(cb, 430, 405, 540, 421, valor(b.getSuperiorInmediato()), texto);

        ColumnText historialPendiente = escribirHistorialInicial(
                cb, valor(b.getHistorial()), texto
        );

        escribirCampo(cb, 455, 40, 560, 55, "Prioridad: " + valor(b.getPrioridad()), texto);

        continuarHistorial(documento, writer, historialPendiente);

		agregarContinuacionSiHaceFalta(
				documento,
				"BOLETÍN DE INVESTIGACIÓN N° " + numero,
				descripcionContinua ? valor(b.getDescripcion()) : null
		);
    }

    public void renderImagenAdjunta(Document documento, PdfWriter writer, Image imagen)
            throws Exception {
        documento.newPage();

        PdfContentByte cb = writer.getDirectContent();
        Font blanco = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 9, Color.WHITE
        );
        barraAzul(cb, 30, 795, 535, 22, "IMÁGENES ADJUNTAS:", blanco);

        imagen.scaleToFit(535, 715);
        float x = (documento.getPageSize().getWidth() - imagen.getScaledWidth()) / 2;
        float y = 55 + (715 - imagen.getScaledHeight()) / 2;
        imagen.setAbsolutePosition(x, y);
        documento.add(imagen);
        writer.setPageEmpty(false);
    }

    public void renderFirmasExpediente(
            Document documento,
            PdfWriter writer,
            List<FirmaExpedienteApiResponse> firmas
    ) throws Exception {
        if (firmas == null || firmas.size() != 2) {
            throw new IllegalArgumentException(
                    "El expediente debe tener exactamente dos firmantes."
            );
        }

        List<FirmaExpedienteApiResponse> ordenadas = firmas.stream()
                .sorted(Comparator.comparingInt(FirmaExpedienteApiResponse::orden))
                .toList();

        documento.newPage();

        Font encabezado = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD, 10, Color.BLACK
        );
        Font nombre = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font area = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);

        PdfPTable tabla = new PdfPTable(3);
        tabla.setTotalWidth(535);
        tabla.setLockedWidth(true);
        tabla.setWidths(new float[] { 1, 1, 1 });

        tabla.addCell(celdaEncabezado("EMISOR:\nLEGAJO:", encabezado));
        tabla.addCell(celdaEncabezado("PUESTO:", encabezado));
        tabla.addCell(celdaEncabezado("", encabezado));

        tabla.addCell(celdaFirmante(ordenadas.get(0), nombre, area));
        PdfPCell centro = new PdfPCell(new Phrase(""));
        centro.setFixedHeight(92);
        tabla.addCell(centro);
        tabla.addCell(celdaFirmante(ordenadas.get(1), nombre, area));

        tabla.writeSelectedRows(0, -1, 30, 760, writer.getDirectContent());
        writer.setPageEmpty(false);
    }

    private PdfPCell celdaEncabezado(String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(AZUL);
        celda.setFixedHeight(32);
        celda.setPaddingLeft(6);
        celda.setPaddingTop(4);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celda;
    }

    private PdfPCell celdaFirmante(
            FirmaExpedienteApiResponse firmante,
            Font fuenteNombre,
            Font fuenteArea
    ) {
        Phrase contenido = new Phrase();
        contenido.add(new Chunk(valor(firmante.nombre()) + "\n", fuenteNombre));
        contenido.add(new Chunk(valor(firmante.areaLinea1()) + "\n", fuenteArea));
        contenido.add(new Chunk(valor(firmante.areaLinea2()), fuenteArea));

        PdfPCell celda = new PdfPCell(contenido);
        celda.setFixedHeight(92);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(8);
        return celda;
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

    private boolean escribirCampo(PdfContentByte cb, float x1, float y1, float x2, float y2,
			String texto, Font fuente) throws Exception {
        ColumnText ct = new ColumnText(cb);
        ct.setSimpleColumn(new Phrase(valor(texto), fuente), x1, y1, x2, y2, 12, Element.ALIGN_LEFT);
		int estado = ct.go();
		return ColumnText.hasMoreText(estado);
    }

    private ColumnText escribirHistorialInicial(PdfContentByte cb, String historial, Font fuente)
            throws Exception {
        ColumnText columna = new ColumnText(cb);
        columna.setSimpleColumn(
                new Phrase(valor(historial), fuente),
                40, 215, 550, 350,
                12,
                Element.ALIGN_JUSTIFIED
        );

        int estado = columna.go();
        return ColumnText.hasMoreText(estado) ? columna : null;
    }

    private void continuarHistorial(Document documento, PdfWriter writer, ColumnText columna)
            throws Exception {
        while (columna != null) {
            documento.newPage();

            PdfContentByte cb = writer.getDirectContent();
            Font blanco = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 9, Color.WHITE
            );

            barraAzul(cb, 30, 795, 535, 22, "HISTORIAL:", blanco);
            cb.rectangle(30, 40, 535, 755);
            cb.stroke();

            columna.setCanvas(cb);
            columna.setSimpleColumn(
                    40, 55, 555, 785,
                    12,
                    Element.ALIGN_JUSTIFIED
            );

            int estado = columna.go();
            if (!ColumnText.hasMoreText(estado)) {
                columna = null;
            }
        }
    }

	private void agregarContinuacionSiHaceFalta(Document documento, String boletin,
			String descripcionCompleta) throws Exception {
		if (descripcionCompleta == null) return;

		documento.newPage();

		Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
		Font subtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
		Font cuerpo = FontFactory.getFont(FontFactory.HELVETICA, 10);

		Paragraph encabezado = new Paragraph("CONTINUACIÓN - " + boletin, titulo);
		encabezado.setAlignment(Element.ALIGN_CENTER);
		encabezado.setSpacingAfter(24);
		documento.add(encabezado);

		if (descripcionCompleta != null) {
			Paragraph etiqueta = new Paragraph("DESCRIPCIÓN COMPLETA", subtitulo);
			etiqueta.setSpacingAfter(8);
			documento.add(etiqueta);

			Paragraph contenido = new Paragraph(descripcionCompleta, cuerpo);
			contenido.setLeading(14);
			contenido.setSpacingAfter(20);
			documento.add(contenido);
		}

	}

    private String valor(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

	private String formatearFecha(LocalDate fecha) {
		return fecha == null ? "" : fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}
    
}
