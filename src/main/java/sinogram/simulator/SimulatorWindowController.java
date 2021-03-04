package sinogram.simulator;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import sinogram.simulator.interfaces.SimulationObserver;
import sinogram.simulator.interfaces.SimulationInterface;

import java.io.File;
import java.io.IOException;

public class SimulatorWindowController implements SimulationObserver {

    private SimulationInterface simulation = new Simulation();
    private double loadedImageHeight;
    private double loadedImageWidth;

    @FXML
    private ImageView loadedImageView;

    @FXML
    private Pane positionLinePane;

    @FXML
    private Label sourceLabel;

    @FXML
    private Line sourcePositionLine;

    @FXML
    private Label detectorLabel;

    @FXML
    private Line detectorPositionLine;

    @FXML
    private ImageView sinogramView;

    @FXML
    private ImageView reconImageView;

    @FXML
    private Button openImageButton;

    @FXML
    private Button startSinogramButton;

    @FXML
    private Button startReconButton;

    @FXML
    private ComboBox<Integer> maxThetaComboBox;

    @FXML
    private ComboBox<Integer> numberOfViewsComboBox;

    @FXML
    private Spinner<Integer> delayInMillisSpinner;

    @FXML
    private Label currentSinogramThetaLabel;

    @FXML
    private Label currentSinogramThetaDisplay;

    @FXML
    private Label currentReconThetaLabel;

    @FXML
    private Label currentReconThetaDisplay;

    public SimulatorWindowController() {

    }

    public void initialize() {
        simulation.registerObserver(this);
    }

    @FXML
    public void openImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open image");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File image = fileChooser.showOpenDialog(loadedImageView.getScene().getWindow());
        if(image != null) {
            loadImage(image);
        }
    }

    public void loadImage(File image) {
        try {
            simulation.setGrayscaleImage(image);
        } catch (IOException e) {
            System.err.println("Invalid file extension. Only PNG, JPG and JPEG files can be loaded.");
        }
    }

    public void updateLoadedImage(Image loadedImage) {
        imageLoadedState(loadedImage);
    }

    @FXML
    public void startSinogramSimulation() {
        simulation.startSinogramSimulation(maxThetaComboBox.getValue(), numberOfViewsComboBox.getValue(), delayInMillisSpinner.getValue());
        simulationInProgressState();
        reconImageView.setImage(null);
    }

    public void updateSinogram(Image sinogram, double currentTheta) {
        Platform.runLater(() -> {
            sinogramView.setImage(sinogram);
            currentSinogramThetaDisplay.setText(Double.toString(currentTheta));
            updatePositionLinePane(currentTheta);
        });
    }

    public void sinogramCompleted() {
        Platform.runLater(this::sinogramCompletedState);
    }

    @FXML
    public void startReconSimulation() {
        simulation.startReconSimulation();
        simulationInProgressState();
    }

    public void updateReconImage(Image reconstructedImage, double currentTheta) {
        Platform.runLater(() -> {
            reconImageView.setImage(reconstructedImage);
            currentReconThetaDisplay.setText(Double.toString(currentTheta));
        });
    }

    public void reconCompleted() {
        Platform.runLater(this::reconCompletedState);
    }

    public void shutdownExecutor() {
        simulation.shutdownExecutor();
    }

    private void updatePositionLinePane(double currentTheta) {
        Rotate rotate = new Rotate();
        rotate.setAngle(360 - currentTheta);
        rotate.setPivotX((loadedImageWidth / 2));
        rotate.setPivotY((loadedImageHeight / 2));
        positionLinePane.getTransforms().clear();
        positionLinePane.getTransforms().add(rotate);
    }

    private void imageLoadedState(Image loadedImage) {
        sinogramView.setImage(null);
        reconImageView.setImage(null);
        loadedImageView.setImage(loadedImage);
        loadedImageHeight = loadedImage.getHeight();
        loadedImageWidth = loadedImage.getWidth();
        startSinogramButton.setDisable(false);
        startReconButton.setDisable(true);
        sourcePositionLine.setVisible(false);
        sourceLabel.setVisible(false);
        detectorPositionLine.setVisible(false);
        detectorLabel.setVisible(false);
        currentSinogramThetaDisplay.setText("");
        currentReconThetaDisplay.setText("");
        drawSourcePositionLine();
        drawDetectorPositionLine();
        updatePositionLinePane(0);
    }

    private void drawSourcePositionLine() {
        sourcePositionLine.setStartX(0);
        sourcePositionLine.setStartY(0);
        sourcePositionLine.setEndX(loadedImageWidth);
        sourcePositionLine.setEndY(0);
        sourceLabel.setTranslateX(loadedImageWidth / 2);
    }

    private void drawDetectorPositionLine() {
        detectorPositionLine.setStartX(0);
        detectorPositionLine.setStartY(loadedImageHeight);
        detectorPositionLine.setEndX(loadedImageWidth);
        detectorPositionLine.setEndY(loadedImageHeight);
        detectorLabel.setTranslateX(loadedImageWidth / 2);
        detectorLabel.setTranslateY(loadedImageHeight);
    }

    private void simulationInProgressState() {
        startSinogramButton.setDisable(true);
        startReconButton.setDisable(true);
        openImageButton.setDisable(true);
        maxThetaComboBox.setDisable(true);
        numberOfViewsComboBox.setDisable(true);
        delayInMillisSpinner.setDisable(true);
        sourcePositionLine.setVisible(true);
        sourceLabel.setVisible(true);
        detectorPositionLine.setVisible(true);
        detectorLabel.setVisible(true);
        currentReconThetaDisplay.setText("");
    }

    private void sinogramCompletedState() {
        startSinogramButton.setDisable(false);
        startReconButton.setDisable(false);
        openImageButton.setDisable(false);
        maxThetaComboBox.setDisable(false);
        numberOfViewsComboBox.setDisable(false);
        delayInMillisSpinner.setDisable(false);
        currentSinogramThetaDisplay.setText("Completed");
    }

    private void reconCompletedState() {
        startSinogramButton.setDisable(false);
        startReconButton.setDisable(false);
        openImageButton.setDisable(false);
        maxThetaComboBox.setDisable(false);
        numberOfViewsComboBox.setDisable(false);
        delayInMillisSpinner.setDisable(false);
        currentReconThetaDisplay.setText("Completed");
    }
}
