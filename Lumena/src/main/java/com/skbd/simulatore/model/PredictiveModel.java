package com.skbd.simulatore.model;

import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.io.Read;
import smile.regression.RandomForest;

public class PredictiveModel {
    private RandomForest predModelEl;
    private DataFrame trainDataEl;

    // Metodo per addestrare il modello all'avvio dell'app
    public void trainModel() {
        try {
            // 1. Carica il dataset (assicurati che il file sia nella cartella giusta)
            trainDataEl = Read.csv("storico_consumi.csv");

            // 2. Definisce cosa prevedere.
            // "consumo_kwh ~ ." significa: prevedi consumo_kwh usando TUTTE le altre colonne
            Formula formula = Formula.lhs("consumo_kwh");

            // 3. Addestra il modello (100 è il numero di alberi decisionali)
            predModelEl = RandomForest.fit(formula, trainDataEl);

            System.out.println("Modello addestrato con successo!");

        } catch (Exception e) {
            System.err.println("Errore durante l'addestramento: " + e.getMessage());
        }
    }

    // Metodo chiamato dal Controller per fare la previsione in tempo reale
    //Ricorda di mettere tutti i parametri necessari
    public double prevediConsumo() {
        if (predModelEl == null) {
            throw new IllegalStateException("The model hasn't been trained");
        }

        // 1. Crea un nuovo record con i parametri scelti dall'utente nell'interfaccia
        Object[] userValues = {0.0};
        // L'ultimo 0.0 è un placeholder per il consumo_kwh che stiamo per calcolare

        Tuple toPredict = Tuple.of(userValues, trainDataEl.schema());

        // 2. Fai la previsione
        return predModelEl.predict(toPredict);
    }
}
