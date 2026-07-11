package controllers;

import java.time.LocalDate;
import java.util.List;

import dao.ReporteDAO;
import dao.ReporteDAO.DatoConteo;
import dao.ReporteDAO.Resumen;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

public class ReportesController {

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Button btnActualizar;
    @FXML private Button btnCerrar;
    @FXML private Label lblTotal;
    @FXML private Label lblPendientes;
    @FXML private Label lblResueltos;
    @FXML private Label lblTiempoPromedio;
    @FXML private BarChart<String, Number> chartAreas;
    @FXML private PieChart chartPrioridades;

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @FXML
    public void initialize() {
        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusMonths(12));
        btnActualizar.setOnAction(e -> actualizar());
        btnCerrar.setOnAction(e -> btnCerrar.getScene().getWindow().hide());
        actualizar();
    }

    private void actualizar() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();

        if (desde == null || hasta == null) {
            mostrarError("Debe seleccionar ambas fechas.");
            return;
        }
        if (desde.isAfter(hasta)) {
            mostrarError("La fecha desde no puede ser posterior a la fecha hasta.");
            return;
        }

        Resumen resumen = reporteDAO.obtenerResumen(desde, hasta);
        lblTotal.setText(String.valueOf(resumen.total()));
        lblPendientes.setText(String.valueOf(resumen.pendientes()));
        lblResueltos.setText(String.valueOf(resumen.resueltos()));
        lblTiempoPromedio.setText(String.format("%.1f h", resumen.horasPromedio()));

        List<DatoConteo> datosAreas = reporteDAO.obtenerPorArea(desde, hasta);
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Incidentes");
        long maximo = 0;
        for (DatoConteo dato : datosAreas) {
            serie.getData().add(new XYChart.Data<>(dato.nombre(), dato.cantidad()));
            maximo = Math.max(maximo, dato.cantidad());
        }
        ObservableList<XYChart.Series<String, Number>> seriesAreas = FXCollections.observableArrayList();
        seriesAreas.add(serie);
        chartAreas.setData(seriesAreas);
        configurarEjeCantidad(maximo);

        ObservableList<PieChart.Data> prioridades = FXCollections.observableArrayList();
        for (DatoConteo dato : reporteDAO.obtenerPorPrioridad(desde, hasta)) {
            prioridades.add(
                    new PieChart.Data(dato.nombre() + " (" + dato.cantidad() + ")", dato.cantidad())
            );
        }
        chartPrioridades.setData(prioridades);
    }

    private void configurarEjeCantidad(long maximo) {
        NumberAxis eje = (NumberAxis) chartAreas.getYAxis();
        double tick = Math.max(1, Math.ceil(maximo / 10.0));
        double limite = Math.max(1, Math.ceil(Math.max(1, maximo) / tick) * tick);

        eje.setAutoRanging(false);
        eje.setLowerBound(0);
        eje.setUpperBound(limite);
        eje.setTickUnit(tick);
        eje.setMinorTickVisible(false);
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Reportes");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
