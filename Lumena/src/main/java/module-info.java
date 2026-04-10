module com.skbd.simulatore {

    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;


    //per smile, il modello predittivo
    requires smile.core;
    requires smile.base;

    opens com.skbd.simulatore.view to javafx.fxml;
    opens com.skbd.simulatore.controller to javafx.fxml;

    exports com.skbd.simulatore.view;
    exports com.skbd.simulatore.controller;
}