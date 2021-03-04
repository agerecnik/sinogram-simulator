package sinogram.simulator;

import sinogram.simulator.interfaces.ReconEventListener;

public class BackProjection {

    private ReconEventListener reconEventListener;

    private double[][] sinogramPixels;
    private double maxTheta;
    private int numberOfViews;
    private int numberOfProjections;

    public BackProjection (double[][] sinogramPixels, double maxTheta, ReconEventListener reconEventListener) {
        this.sinogramPixels = sinogramPixels;
        this.maxTheta = maxTheta;
        this.reconEventListener = reconEventListener;
        this.numberOfViews = sinogramPixels.length;
        this.numberOfProjections = sinogramPixels[0].length;
    }

    public void reconstructImage() {
        int reconstructionSize = (int) Math.floor(numberOfProjections / Math.sqrt(2));
        double[][] reconstructionPixels = new double[reconstructionSize][reconstructionSize];

        for(int view = 0; view < numberOfViews; view++) {
            double theta = view * maxTheta / numberOfViews;
            double sinTheta = Math.sin(theta * Math.PI / 180);
            double cosTheta = Math.cos(theta * Math.PI / 180);
            for(int projection = 0; projection < numberOfProjections; projection++) {
                double r = Math.floor(projection - numberOfProjections / 2.0);
                for(double x = -reconstructionSize / 2.0 + 1; x < reconstructionSize / 2.0; x++) {
                    for(double y = -reconstructionSize / 2.0 + 1; y < reconstructionSize / 2.0; y++) {
                        if(Math.round(x * cosTheta + y * sinTheta - r) == 0) {
                            int recX = (int) Math.floor(x + reconstructionSize / 2.0);
                            int recY = (int) Math.floor(-y + reconstructionSize / 2.0);
                            reconstructionPixels[recX][recY] = reconstructionPixels[recX][recY] + sinogramPixels[view][projection];
                        }
                    }
                }
            }
            reconEventListener.reconViewCompleted(reconstructionPixels, theta);
        }
        reconEventListener.reconCompleted();
    }
}
