package sinogram.simulator.interfaces;

import java.io.File;
import java.io.IOException;

public interface SimulationInterface {

    void setGrayscaleImage(File image) throws IOException;

    void startSinogramSimulation(double maxTheta, int numberOfViews, int delay);

    void startReconSimulation();

    void registerObserver(SimulationObserver o);

    void removeObserver(SimulationObserver o);

    void shutdownExecutor();
}
