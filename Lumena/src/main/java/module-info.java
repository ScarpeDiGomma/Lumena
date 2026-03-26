module com.skbd.simulatore {
    requires javafx.controls;
    requires javafx.fxml;

    //per sql
    requires java.sql;

    //per smile, il modello predittivo
    requires smile.core;
    //requires smile.math;
    //requires smile.data;

    opens com.skbd.simulatore.view to javafx.fxml;

    exports com.skbd.simulatore.view;
}