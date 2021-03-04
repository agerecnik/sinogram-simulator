package sinogram.simulator;

import sinogram.simulator.interfaces.SinogramEventListener;

public class RadonTransform {

    private SinogramEventListener sinogramEventListener;
    private int delayInMillis;

    private double[][] grayscaleImagePixels;
    private double[][] sinogramPixels;
    private int imageWidth;
    private int imageHeight;
    private int xImageCenter;
    private int yImageCenter;
    private int ro;
    private int numberOfBins;
    private int numberOfViews;
    private double maxTheta;
    private double angularIncrement;

    public RadonTransform(double[][] grayscaleImagePixels, double maxTheta, int numberOfViews, SinogramEventListener listener, int delayInMillis) {
        sinogramEventListener = listener;
        this.delayInMillis = delayInMillis;
        this.grayscaleImagePixels = grayscaleImagePixels;
        imageWidth = grayscaleImagePixels.length;
        imageHeight = grayscaleImagePixels[0].length;
        xImageCenter = (int) Math.floor(imageWidth / 2.0);
        yImageCenter = (int) Math.floor(imageHeight / 2.0);
        ro = (int) Math.ceil(Math.sqrt(Math.pow(imageWidth-xImageCenter, 2) + Math.pow(imageHeight-yImageCenter, 2))) + 1;
        numberOfBins = 2 * ro + 1;
        this.maxTheta = maxTheta;
        this.numberOfViews = numberOfViews;
        angularIncrement = maxTheta / numberOfViews;
    }

    public void calculateSinogram() {
        sinogramPixels = new double[numberOfViews][numberOfBins];
        for(double theta = 0; theta < maxTheta; theta = theta + angularIncrement) {
            double sinTheta = Math.sin(theta * Math.PI / 180);
            double cosTheta = Math.cos(theta * Math.PI / 180);
            calculateProjections(theta, sinTheta, cosTheta);
            sinogramViewCompleted(theta);
            delayBetweenViews(delayInMillis);
        }
        sinogramCompleted();
    }

    private void calculateProjections(double theta, double sinTheta, double cosTheta) {
        for(int x = 0; x < imageWidth; x++) {
            double xcos1 = (x - xImageCenter + 0.25) * cosTheta;
            double xcos2 = (x - xImageCenter - 0.25) * cosTheta;
            for(int y = 0; y < imageHeight; y++) {
                double ysin1 = (yImageCenter - y + 0.25) * sinTheta;
                double ysin2 = (yImageCenter - y - 0.25) * sinTheta;
                double subPixelValue = grayscaleImagePixels[x][y] * 0.25;

                double subpixel1 = xcos1 + ysin1 + ro;
                addSubpixelValueToTwoNearestBins(subpixel1, theta, subPixelValue);

                double subpixel2 = xcos2 + ysin1 + ro;
                addSubpixelValueToTwoNearestBins(subpixel2, theta, subPixelValue);

                double subpixel3 = xcos1 + ysin2 + ro;
                addSubpixelValueToTwoNearestBins(subpixel3, theta, subPixelValue);

                double subpixel4 = xcos2 + ysin2 + ro;
                addSubpixelValueToTwoNearestBins(subpixel4, theta, subPixelValue);
            }
        }
    }

    private void addSubpixelValueToTwoNearestBins(double subPixel, double theta, double subPixelValue) {
        double bin = Math.floor(subPixel);
        double delta = subPixel - bin;
        int projectionBin1 = (int) bin;
        int projectionBin2 = (int) (bin + 1);
        int view = (int) (theta / angularIncrement);
        sinogramPixels[view][projectionBin1] = sinogramPixels[view][projectionBin1] + subPixelValue * (1 - delta);
        sinogramPixels[view][projectionBin2] = sinogramPixels[view][projectionBin2] + subPixelValue * delta;
    }

    private void delayBetweenViews(int delayInMillis) {
        try {
            Thread.sleep(delayInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sinogramViewCompleted(double currentTheta) {
        sinogramEventListener.sinogramViewCompleted(sinogramPixels, currentTheta);
    }

    private void sinogramCompleted() {
        sinogramEventListener.sinogramCompleted(sinogramPixels, maxTheta);
    }
}

