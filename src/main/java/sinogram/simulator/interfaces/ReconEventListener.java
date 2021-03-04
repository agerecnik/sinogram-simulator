package sinogram.simulator.interfaces;

public interface ReconEventListener {

    void reconViewCompleted(double[][] reconPixels, double currentTheta);

    void reconCompleted();
}
