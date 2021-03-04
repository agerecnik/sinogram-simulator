package sinogram.simulator.interfaces;

import javafx.scene.image.Image;

public interface SimulationObserver {

    void updateLoadedImage(Image loadedImage);

    void updateSinogram(Image sinogram, double currentTheta);

    void sinogramCompleted();

    void updateReconImage(Image reconstructedImage, double currentTheta);

    void reconCompleted();
}
