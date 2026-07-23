module SistemaIncidentes {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    
    requires com.github.librepdf.openpdf;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    exports app;

    opens controllers to javafx.fxml;
    opens models to javafx.base;
    opens api to com.fasterxml.jackson.databind;
}
