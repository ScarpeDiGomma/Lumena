package com.skbd.simulatore.controller;

import javafx.event.ActionEvent;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {
    public void handleLogin(ActionEvent actionEvent) {

        //Mettere tutto il codice relativo al login


        try {
            // 1. Carica il nuovo file FXML
            // Attenzione al percorso! Inizia con "/" e deve rispecchiare la cartella in src/main/resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/skbd/simulatore/view/map.fxml"));
            Parent root = loader.load();

            // 2. Crea la nuova Scena
            Scene newScene = new Scene(root);

            // 3. Ottieni lo Stage (la finestra attuale) partendo dall'evento del clic
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // 4. Cambia la scena della finestra e mostrala
            stage.setScene(newScene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Page could not be loaded: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void handleGoToRegister(ActionEvent actionEvent) {
        try {
            // 1. Carica il nuovo file FXML
            // Attenzione al percorso! Inizia con "/" e deve rispecchiare la cartella in src/main/resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/skbd/simulatore/view/register.fxml"));
            Parent root = loader.load();

            // 2. Crea la nuova Scena
            Scene newScene = new Scene(root);

            // 3. Ottieni lo Stage (la finestra attuale) partendo dall'evento del clic
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

            // 4. Cambia la scena della finestra e mostrala
            stage.setScene(newScene);
            stage.show();

        } catch (IOException e) {
            System.err.println("Page could not be loaded: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
