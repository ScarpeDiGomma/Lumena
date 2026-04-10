package com.skbd.simulatore.controller;

import com.skbd.simulatore.model.PredictiveModel;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.SVGPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;

import java.io.IOException;
import java.net.URL;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for map.fxml.
 *
 * Responsibilities:
 *  • Populate Year / Month ComboBoxes
 *  • Inject LineCharts into the chart placeholder Panes
 *  • Animate the prediction drawer (slide in / out)
 *  • Respond to region selection on the Italy map
 *  • Navigate back to the previous screen
 */
public class MapController implements Initializable {

    // ── Main layer ────────────────────────────────────────────
    @FXML private AnchorPane mainLayer;

    // ── Details panel ─────────────────────────────────────────
    @FXML private Label      regionNameLabel;
    @FXML private ComboBox<String> yearComboBox;
    @FXML private ComboBox<String> monthComboBox;

    /** Placeholder Pane for the electricity LineChart (main panel). */
    @FXML private Pane electricityChartContainer;

    /** Placeholder Pane for the gas LineChart (main panel). */
    @FXML private Pane gasChartContainer;

    // ── Prediction drawer ─────────────────────────────────────
    @FXML private AnchorPane predictionDrawer;

    @FXML private Label drawerElectricityData;
    @FXML private Label drawerGasData;
    @FXML private Label drawerSavingsData;

    @FXML private Pane drawerElectricityChart;
    @FXML private Pane drawerGasChart;
    @FXML private Pane drawerSavingsChart;

    //Relative to the user image
    @FXML private StackPane userAvatarPane;
    @FXML private ImageView userAvatarImage; // L'ImageView che abbiamo aggiunto
    @FXML private Label userAvatarLabel;       // La label esistente

    // ── Animation constants ───────────────────────────────────
    private static final double ANIMATION_MS      = 400.0;
    private static final double DRAWER_HIDDEN_X   = 1280.0; // must match FXML translateX

    // ── Currently selected region ─────────────────────────────
    private String selectedRegion = "VENETO";

    //Map container
    @FXML private Pane mapContainer;

    // Predictive model
    private PredictiveModel preModel = new PredictiveModel();

    // ─────────────────────────────────────────────────────────
    //  INITIALISE
    // ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Ensure drawer starts fully off-screen to the right
        predictionDrawer.setTranslateX(DRAWER_HIDDEN_X);

        populateYearComboBox();
        populateMonthComboBox();
        injectCharts();

        //Train the predictive model
        //preModel.trainModel();

        //Load the map
        loadMap();

        //Initialize all images
        initializeImages();
    }

    private void initializeImages() {
        initializeUserIcon();
    }

    private void initializeUserIcon() {
        // --- STEP 1: Rendere l'immagine circolare (Il Clipping) ---
        // Creiamo un cerchio. Il raggio deve essere la metà della dimensione dell'immagine (fitWidth/2).
        // Assicurati che fitWidth e fitHeight siano uguali nell'FXML.
        Circle clip = new Circle(20, 20, 20); // centerX, centerY, radius
        userAvatarImage.setClip(clip);

        // --- STEP 2: Caricare l'immagine (Esempio) ---
        // Immaginiamo di caricare l'immagine dell'utente.
        // Se non c'è un'immagine, mostriamo la label di fallback.

        // Esempio: carichiamo un'immagine di test
        Image imgUtente = new Image(getClass().getResourceAsStream("/com/skbd/simulatore/Images/userIcon.png"));


        if (imgUtente != null) {
            userAvatarImage.setImage(imgUtente);
            userAvatarImage.setVisible(true); // Mostriamo l'immagine
            userAvatarLabel.setVisible(false);    // Nascondiamo la label "IMG"
        } else {
            // Se l'immagine manca, mostriamo il fallback circolare con la label
            userAvatarImage.setVisible(false);
            userAvatarLabel.setVisible(true);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  COMBO-BOX POPULATION
    // ─────────────────────────────────────────────────────────

    private void populateYearComboBox() {
        List<String> years = new ArrayList<>();
        int currentYear = Year.now().getValue();
        for (int y = currentYear; y >= currentYear - 10; y--) {
            years.add(String.valueOf(y));
        }
        yearComboBox.setItems(FXCollections.observableArrayList(years));
        yearComboBox.getSelectionModel().selectFirst();
    }

    private void populateMonthComboBox() {
        monthComboBox.setItems(FXCollections.observableArrayList(
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        ));
        monthComboBox.getSelectionModel().selectFirst();
    }

    // ─────────────────────────────────────────────────────────
    //  CHART INJECTION
    // ─────────────────────────────────────────────────────────

    /**
     * Builds LineCharts and injects them into the placeholder Panes.
     * Called once on initialize; call again after filter changes if
     * you want to refresh data from a service.
     */
    private void injectCharts() {
        injectLineChart(electricityChartContainer, buildElectricityData(), "kWh");
        injectLineChart(gasChartContainer,         buildGasData(),         "m³");
        injectLineChart(drawerElectricityChart,    buildPredictedElectricityData(), "kWh (predicted)");
        injectLineChart(drawerGasChart,            buildPredictedGasData(),         "m³ (predicted)");
        injectLineChart(drawerSavingsChart,        buildSavingsData(),              "€ saved");
    }

    /**
     * Creates a styled LineChart and attaches it to the given container Pane,
     * stretching it to fill the available space via AnchorPane constraints.
     */
    private void injectLineChart(Pane container,
                                 XYChart.Series<String, Number> series,
                                 String yLabel) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        yAxis.setLabel(yLabel);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.getData().add(series);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("lumena-chart");

        // Stretch chart to fill placeholder
        chart.prefWidthProperty().bind(container.widthProperty());
        chart.prefHeightProperty().bind(container.heightProperty());

        container.getChildren().setAll(chart);
    }

    // ── Dummy data builders ───────────────────────────────────
    //  Replace these with calls to your service / repository layer.

    private XYChart.Series<String, Number> buildElectricityData() {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Electricity");
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                           "Jul","Aug","Sep","Oct","Nov","Dec"};
        double[] values = {320, 290, 270, 250, 230, 260, 310, 340, 300, 280, 295, 330};
        for (int i = 0; i < months.length; i++) {
            s.getData().add(new XYChart.Data<>(months[i], values[i]));
        }
        return s;
    }

    private XYChart.Series<String, Number> buildGasData() {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Gas");
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun",
                           "Jul","Aug","Sep","Oct","Nov","Dec"};
        double[] values = {180, 170, 140, 100, 60, 40, 35, 38, 70, 110, 150, 175};
        for (int i = 0; i < months.length; i++) {
            s.getData().add(new XYChart.Data<>(months[i], values[i]));
        }
        return s;
    }

    private XYChart.Series<String, Number> buildPredictedElectricityData() {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Predicted Electricity");
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun"};
        double[] values = {315, 285, 265, 245, 225, 255};
        for (int i = 0; i < months.length; i++) {
            s.getData().add(new XYChart.Data<>(months[i], values[i]));
        }
        return s;
    }

    private XYChart.Series<String, Number> buildPredictedGasData() {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Predicted Gas");
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun"};
        double[] values = {175, 165, 135, 95, 55, 38};
        for (int i = 0; i < months.length; i++) {
            s.getData().add(new XYChart.Data<>(months[i], values[i]));
        }
        return s;
    }

    private XYChart.Series<String, Number> buildSavingsData() {
        XYChart.Series<String, Number> s = new XYChart.Series<>();
        s.setName("Savings");
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun"};
        double[] values = {50, 65, 80, 95, 110, 130};
        for (int i = 0; i < months.length; i++) {
            s.getData().add(new XYChart.Data<>(months[i], values[i]));
        }
        return s;
    }

    // ─────────────────────────────────────────────────────────
    //  FXML EVENT HANDLERS
    // ─────────────────────────────────────────────────────────

    /**
     * Called when the user changes the YEAR or MONTH ComboBox.
     * Refresh charts / data here.
     */
    @FXML
    private void handleFilterChange() {
        String year  = yearComboBox.getValue();
        String month = monthComboBox.getValue();
        if (year == null || month == null) return;

        // TODO: fetch filtered data from service and rebuild charts
        // For now just re-inject the same dummy data
        injectCharts();
    }

    /**
     * Called when the user clicks the PREDICTION button.
     * Slides the prediction drawer in from the right.
     */
    @FXML
    private void handlePrediction() {
        // Populate drawer with prediction data before animating
        populateDrawer();
        slideDrawer(0.0);
    }

    /**
     * Called when the user clicks the back arrow inside the drawer.
     * Slides the drawer back off-screen to the right.
     */
    @FXML
    private void handleCloseDrawer() {
        slideDrawer(DRAWER_HIDDEN_X);
    }

    /**
     * Called when the user clicks the back arrow on the main layer.
     * Navigate back to the previous scene (e.g. a region-selection or
     * home screen). Replace the body of this method with your
     * SceneManager / Navigator call.
     */
    @FXML
    private void handleBack(ActionEvent actionEvent) {
        try {
            // 1. Carica il nuovo file FXML
            // Attenzione al percorso! Inizia con "/" e deve rispecchiare la cartella in src/main/resources
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/skbd/simulatore/view/login.fxml"));
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

    /**
     * Called when the user clicks the avatar circle.
     * Show a profile pop-up or navigate to a settings screen.
     */
    @FXML
    private void handleUserAvatar() {
        // TODO: show user profile / settings
        System.out.println("[MapController] Avatar clicked");
    }

    // ─────────────────────────────────────────────────────────
    //  PUBLIC API — called by the map component when a region
    //  is tapped / clicked
    // ─────────────────────────────────────────────────────────

    /**
     * Update the details panel to reflect the newly selected region.
     * Call this from your map interaction layer (SVGView, WebView JS
     * bridge, or a custom canvas component).
     *
     * @param regionName Display name of the selected region (e.g. "VENETO")
     */
    public void selectRegion(String regionName) {
        this.selectedRegion = regionName;
        regionNameLabel.setText(regionName.toUpperCase());

        // Reset drawer to hidden state whenever the region changes
        predictionDrawer.setTranslateX(DRAWER_HIDDEN_X);

        // Refresh charts for the new region
        injectCharts();
    }

    // ─────────────────────────────────────────────────────────
    //  PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Animate the prediction drawer to the given translateX target.
     *
     * @param targetX 0.0 to show the drawer; DRAWER_HIDDEN_X to hide it.
     */
    private void slideDrawer(double targetX) {
        TranslateTransition tt = new TranslateTransition(
                Duration.millis(ANIMATION_MS), predictionDrawer);
        tt.setToX(targetX);
        tt.play();
    }

    /**
     * Populate the drawer labels and charts with prediction data for
     * the current region / filter selection.
     * Replace the placeholder text with real data from your service.
     */
    private void populateDrawer() {
        String year  = yearComboBox.getValue() != null ? yearComboBox.getValue()  : "—";
        String month = monthComboBox.getValue() != null ? monthComboBox.getValue() : "—";

        drawerElectricityData.setText(
                "Predicted electricity consumption for " + selectedRegion
                + " — " + month + " " + year + ".\n"
                + "Estimated: 265 kWh  |  Δ vs last year: −8%");

        drawerGasData.setText(
                "Predicted gas consumption for " + selectedRegion
                + " — " + month + " " + year + ".\n"
                + "Estimated: 138 m³  |  Δ vs last year: −5%");

        drawerSavingsData.setText(
                "Estimated future savings based on current consumption trends.\n"
                + "Projected annual saving: €1,240  |  CO₂ reduction: 320 kg");

        // Rebuild drawer charts (could swap in prediction-specific data here)
        injectLineChart(drawerElectricityChart, buildPredictedElectricityData(), "kWh (predicted)");
        injectLineChart(drawerGasChart,         buildPredictedGasData(),         "m³ (predicted)");
        injectLineChart(drawerSavingsChart,      buildSavingsData(),              "€ saved");
    }

    private void loadMap() {
        // Svuota il contenitore nel caso venga richiamato più volte
        mapContainer.getChildren().clear();

        try {
            // 1. Carichiamo il file SVG come risorsa
            InputStream svgFile = getClass().getResourceAsStream("/com/skbd/simulatore/Images/it.svg");

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(svgFile);

            // 2. Prendiamo tutti i tag <path> (le regioni)
            NodeList paths = doc.getElementsByTagName("path");

            for (int i = 0; i < paths.getLength(); i++) {
                Element pathElement = (Element) paths.item(i);

                // Estraiamo le coordinate (d) e il nome (id)
                String d = pathElement.getAttribute("d");
                String name = pathElement.getAttribute("name").toUpperCase(); // es: "VENETO"

                // 3. Creiamo l'oggetto grafico JavaFX
                SVGPath regionPath = new SVGPath();
                regionPath.setContent(d);
                regionPath.getStyleClass().add("map-region");

                // Impostiamo l'evento al clic
                regionPath.setOnMouseClicked(event -> selectRegion(name));

                // Aggiungiamo la regione al contenitore nel file FXML
                mapContainer.getChildren().add(regionPath);
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento della mappa SVG: " + e.getMessage());
            e.printStackTrace();
        }

        // Opzionale: centra la mappa nel contenitore
        // mapContainer.setTranslateX(50);
        // mapContainer.setTranslateY(50);
    }
}
