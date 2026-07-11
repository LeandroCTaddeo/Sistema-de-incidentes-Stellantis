package services;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import dao.BoletinAdminDAO;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import models.BoletinAdmin;
import models.Incidente;

public class PDFService {

    private BoletinAdminDAO boletinDAO = new BoletinAdminDAO();

    public void exportarExpediente(Incidente incidente, Window ventana) {

        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Guardar expediente");
            chooser.setInitialFileName("Expediente_" + incidente.getId() + ".pdf");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF", "*.pdf")
            );

            File archivo = chooser.showSaveDialog(ventana);

            if (archivo == null) {
                return;
            }

            Document documento = new Document(PageSize.A4, 30, 30, 20, 20);

            PdfWriter writer = PdfWriter.getInstance(
                    documento,
                    new FileOutputStream(archivo)
            );

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

            documento.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}