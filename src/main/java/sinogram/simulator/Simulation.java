package sinogram.simulator;

import javafx.scene.image.WritableImage;
import sinogram.simulator.interfaces.ReconEventListener;
import sinogram.simulator.interfaces.SinogramEventListener;
import sinogram.simulator.interfaces.SimulationObserver;
import sinogram.simulator.interfaces.SimulationInterface;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.embed.swing.SwingFXUtils;


public class Simulation implements SimulationInterface, SinogramEventListener, ReconEventListener {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private List<SimulationObserver> simulationObservers = new ArrayList<>();
    private BufferedImage grayscaleImage;
    private RadonTransform radonTransform;
    private BackProjection backProjection;

    private final int MAX_IMAGE_WIDTH = 300;
    private final int MAX_IMAGE_HEIGHT = 300;


    public void setGrayscaleImage(File originalImage) throws IOException {
        grayscaleImage = rescaleAndConvertToGrayscale(ImageIO.read(originalImage));
        WritableImage grayscaleImageFX = SwingFXUtils.toFXImage(grayscaleImage, null);
        simulationObservers.forEach(observer -> observer.updateLoadedImage(grayscaleImageFX));
    }

    public void startSinogramSimulation(double maxTheta, int numberOfViews, int delayInMillis) {
        double[][] grayscaleImagePixels = convertImageToArray(grayscaleImage);
        radonTransform = new RadonTransform(grayscaleImagePixels, maxTheta, numberOfViews, this, delayInMillis);
        executor.execute(() -> radonTransform.calculateSinogram());
    }

    public void sinogramViewCompleted(double[][] sinogramPixels, double currentTheta) {
        WritableImage sinogramFX = SwingFXUtils.toFXImage(createImage(sinogramPixels), null);
        simulationObservers.forEach(observer -> observer.updateSinogram(sinogramFX, currentTheta));
    }

    public void sinogramCompleted(double[][] sinogramPixels, double maxTheta) {
        backProjection = new BackProjection(sinogramPixels, maxTheta, this);
        simulationObservers.forEach(SimulationObserver::sinogramCompleted);
    }

    public void startReconSimulation() {
        if(backProjection != null) {
            executor.execute(() -> backProjection.reconstructImage());
        }
    }

    public void reconViewCompleted(double[][] reconPixels, double currentTheta) {
        WritableImage reconImageFX = SwingFXUtils.toFXImage(createImage(reconPixels), null);
        simulationObservers.forEach(observer -> observer.updateReconImage(reconImageFX, currentTheta));
    }

    public void reconCompleted() {
        simulationObservers.forEach(SimulationObserver::reconCompleted);
    }

    public void registerObserver(SimulationObserver observer) {
        simulationObservers.add(observer);
    }

    public void removeObserver(SimulationObserver observer) {
        simulationObservers.remove(observer);
    }

    public void shutdownExecutor() {
        executor.shutdown();
    }

    private BufferedImage rescaleAndConvertToGrayscale(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        if(width > MAX_IMAGE_WIDTH && width >= height) {
            double ratio = (double) height / width;
            width = MAX_IMAGE_WIDTH;
            height = (int) Math.round(MAX_IMAGE_WIDTH * ratio);
        } else if(height > MAX_IMAGE_HEIGHT) {
            double ratio = (double) width / height;
            height = MAX_IMAGE_HEIGHT;
            width = (int) Math.round(MAX_IMAGE_HEIGHT * ratio);
        }

        BufferedImage resizedGrayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resizedGrayscaleImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedGrayscaleImage;
    }

    private double[][] convertImageToArray(BufferedImage image) {
        Raster grayscaleImageData = image.getData();
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] grayscaleImagePixels = new double[width][height];
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                grayscaleImagePixels[x][y] = grayscaleImageData.getSample(x, y, 0);
            }
        }
        return grayscaleImagePixels;
    }

    private BufferedImage createImage(double[][] pixels) {
        int width = pixels.length;
        int height = pixels[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster imageRaster = image.getRaster();
        double maxPixelValue = findMaxPixelValue(width, height, pixels);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double pixelValue = pixels[x][y] * 255 / maxPixelValue;
                imageRaster.setSample(x, y, 0, pixelValue);
            }
        }
        return image;
    }

    private double findMaxPixelValue(int width, int height, double[][] pixels) {
        double maxPixelValue = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(pixels[x][y] > maxPixelValue) {
                    maxPixelValue = pixels[x][y];
                }
            }
        }
        return maxPixelValue;
    }
}
