package com.skbd.simulatore.model;

import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.formula.Formula;
import smile.io.Read;
import smile.regression.RandomForest;
import org.apache.commons.csv.CSVFormat;

import java.io.InputStream;
import java.net.URL;

public class PredictiveModel {
    private RandomForest predModelEl;
    private DataFrame trainDataEl;
    private boolean isUsable;

    public PredictiveModel() {
        isUsable = false;
    }

    //Per l'agenzia delle entrate
    /*
    ABRUZZO	                1	MOLISE	        12
    BASILICATA          	2	PIEMONTE	    13
    BOLZANO	                3	PUGLIA      	14
    CALABRIA	            4	SARDEGNA    	15
    CAMPANIA	            5	SICILIA     	16
    EMILIA ROMAGNA	        6	TOSCANA     	17
    FRIULI VENEZIA GIULIA	7	TRENTO	        18  (Trentino-Alto Adige)
    LAZIO	                8	UMBRIA      	19
    LIGURIA	                9	VALLE D'AOSTA	20
    LOMBARDIA	            10	VENETO	        21
    MARCHE	                11




    AT = 3
    MT = 2
    ST = 1
     */

    // Metodo per addestrare il modello all'avvio dell'app
    public void trainModel() {
        try {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setDelimiter(',')
                    .setHeader() // Indica che deve leggere i nomi dalle colonne
                    .setSkipHeaderRecord(true) // Salta la riga dei nomi durante la lettura dei dati
                    .build();


            // 1. Carica il dataset
            trainDataEl = Read.csv("W:\\GPOI\\5\\ProgettoPredizioneEnergetica\\Lumena\\Lumena\\src\\main\\java\\com\\skbd\\simulatore\\model\\electricity2019.csv", format);
            //TODO: i dati relativi alla temperatura presenti nel dataset non solo reali
            //TODO: addestrare il modello con tutti i dati, anche da file diversi
            //TODO: salvare il modello una volta che viene addestrato
            //TODO: per ogni mese ho il consumo del mese e l'indicatore calcolato da quel mese; l'indicatore dovrebbe andare nei mesi successivi (almeno in quello subito dopo) dato che è un indicatore di crescita: se voglio prevedere i consumi futuri gli metto l'indicatore di crescita futura con i dati del mese il cui consumo voglio prevedere, anche se l'indicatore è stato calcolato basandosi su questo mese

            //TODO: Per ora non tengo conto della classe, deve essere convertita in numero
            trainDataEl = trainDataEl.select("Year", "Month", "Region", "Tension_Type", "Indicator", "Consumption_Gwh", "T_avg", "T_max", "T_min", "Hot_days", "Cold_days", "El_price", "Event", "Eff_intervention");


            // 2. Definisce cosa prevedere
            Formula formula = Formula.lhs("Consumption_Gwh");

            // 3. Addestra il modello (100 è il numero di alberi decisionali)
            predModelEl = RandomForest.fit(formula, trainDataEl);

            System.out.println("Model trained correctly");
            isUsable = true;

        } catch (Exception e) {
            System.err.println("Error during the models training: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metodo chiamato dal Controller per fare la previsione in tempo reale
    public double predict(int year, int monthNumber, int regionNumber, int tensionType, double indicator, int tAVG, int tMAX, int tMIN, int hotDays, int coldDays, double elPrice, int event, int effIntervention) {
        if (predModelEl == null) {
            throw new IllegalStateException("The model hasn't been trained");
        }

        // 1. Crea un nuovo record con i parametri scelti dall'utente nell'interfaccia
        Object[] userValues = {year, monthNumber, regionNumber, tensionType, indicator, 0.0, tAVG, tMAX, tMIN, hotDays, coldDays, elPrice, event, effIntervention};
        // L'ultimo 0.0 è un placeholder per il consumo_kwh che stiamo per calcolare

        Tuple toPredict = Tuple.of(userValues, trainDataEl.schema());

        // 2. Fai la previsione
        return predModelEl.predict(toPredict);
    }

    public boolean isUsable() {
        return isUsable;
    }
}
