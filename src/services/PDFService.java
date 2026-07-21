package services;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import dao.BoletinAdminDAO;
import dao.ImagenDAO;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.BoletinAdmin;
import models.Incidente;
import models.Imagen;

public class PDFService {

    private BoletinAdminDAO boletinDAO = new BoletinAdminDAO();
    private ImagenDAO imagenDAO = new ImagenDAO();
    private AlmacenamientoImagenService almacenamientoImagenes =
            new AlmacenamientoImagenService();

    public boolean exportarExpediente(Incidente incidente, Window ventana) throws Exception {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar expediente");
        chooser.setInitialFileName("Expediente_" + incidente.getId() + ".pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf")
        );

        File archivo = chooser.showSaveDialog(ventana);

        if (archivo == null) {
            return false;
        }

		Path destino = archivo.toPath().toAbsolutePath();
		Path carpetaDestino = destino.getParent();
		Path temporal = Files.createTempFile(carpetaDestino, ".expediente-", ".tmp");
		boolean completado = false;

		try {
			generarExpediente(incidente, temporal);
			moverResultado(temporal, destino);
			completado = true;
		} finally {
			if (!completado) Files.deleteIfExists(temporal);
		}

		return true;
	}

	private void generarExpediente(Incidente incidente, Path archivoTemporal) throws Exception {
		try (FileOutputStream salida = new FileOutputStream(archivoTemporal.toFile())) {
            Document documento = new Document(PageSize.A4, 30, 30, 20, 20);
            try {
                PdfWriter writer = PdfWriter.getInstance(documento, salida);
                documento.open();

                FormularioPDFRenderer renderer = new FormularioPDFRenderer();
                renderer.renderBoletinEmpleado(documento, writer, incidente);

                List<BoletinAdmin> boletines =
                        boletinDAO.obtenerPorIncidente(incidente.getId());

                int numero = 1;
                for (BoletinAdmin boletin : boletines) {
                    documento.newPage();
                    renderer.renderBoletinAdministrador(documento, writer, boletin, numero);
                    numero++;
                }

                agregarImagenes(documento, incidente.getId());
                agregarFinExpediente(documento, incidente.getId());
            } finally {
                if (documento.isOpen()) {
                    documento.close();
                }
            }
        }
    }

	private void moverResultado(Path temporal, Path destino) throws Exception {
		try {
			Files.move(
					temporal,
					destino,
					StandardCopyOption.ATOMIC_MOVE,
					StandardCopyOption.REPLACE_EXISTING
			);
		} catch (AtomicMoveNotSupportedException e) {
			Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
		}
	}

    private void agregarImagenes(Document documento, int incidenteId) throws Exception {
        List<Imagen> imagenes = imagenDAO.obtenerPorIncidente(incidenteId);
        int numero = 1;

        for (Imagen adjunto : imagenes) {
            File archivoImagen;
            try {
                archivoImagen = almacenamientoImagenes.resolver(adjunto.getRuta());
            } catch (StorageException e) {
				throw new StorageException(
						"La imagen adjunta N° " + numero + " tiene una referencia inválida.",
						e
				);
            }
            if (!archivoImagen.isFile()) {
				throw new StorageException(
						"No se encontró la imagen adjunta N° " + numero + "."
				);
            }

            documento.newPage();

            Paragraph titulo = new Paragraph(
                    "IMAGEN ADJUNTA N° " + numero,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15)
            );
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(15);
            documento.add(titulo);

			com.lowagie.text.Image imagen;
			try {
				imagen = com.lowagie.text.Image.getInstance(archivoImagen.getAbsolutePath());
			} catch (Exception e) {
				throw new StorageException(
						"No se pudo incorporar la imagen adjunta N° " + numero + " al expediente.",
						e
				);
			}
            imagen.scaleToFit(535, 720);
            imagen.setAlignment(Element.ALIGN_CENTER);
            documento.add(imagen);

            numero++;
        }
    }

    private void agregarFinExpediente(Document documento, int incidenteId) throws Exception {
        documento.newPage();

        Font fuente = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph fin = new Paragraph(
                "FIN DEL EXPEDIENTE N° " + incidenteId,
                fuente
        );
        fin.setAlignment(Element.ALIGN_CENTER);
        fin.setSpacingBefore(340);
        documento.add(fin);
    }
}
