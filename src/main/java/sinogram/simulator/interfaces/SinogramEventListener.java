package sinogram.simulator.interfaces;

public interface SinogramEventListener {

    void sinogramViewCompleted(double[][] sinogramPixels, double currentTheta);

    void sinogramCompleted(double[][] sinogramPixels, double maxTheta);
}