module SistemaIncidentes {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    
    requires com.github.librepdf.openpdf;

    exports app;

    opens controllers to javafx.fxml;
    opens models to javafx.base;
}