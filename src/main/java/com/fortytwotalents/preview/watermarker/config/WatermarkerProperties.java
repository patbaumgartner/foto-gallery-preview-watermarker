package com.fortytwotalents.preview.watermarker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "watermarker")
public class WatermarkerProperties {

    /**
     * Path to the input folder containing original images.
     */
    private String inputFolder = ".";

    /**
     * Path to the output folder where processed images will be written.
     * The directory structure mirrors the input folder.
     */
    private String outputFolder = "./output";

    /**
     * Resize factor: output image dimensions are divided by this value.
     * For example, a factor of 10 reduces each dimension to 1/10th of the original.
     */
    private double resizeFactor = 10.0;

    /**
     * Path or classpath resource for the watermark logo PNG.
     * Supports "classpath:" prefix for resources bundled in the JAR.
     */
    private String watermarkLogo = "classpath:logo.png";

    /**
     * Font size (in points) used to render the filename text on each image.
     */
    private float filenameFontSize = 12.0f;

    public String getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public double getResizeFactor() {
        return resizeFactor;
    }

    public void setResizeFactor(double resizeFactor) {
        this.resizeFactor = resizeFactor;
    }

    public String getWatermarkLogo() {
        return watermarkLogo;
    }

    public void setWatermarkLogo(String watermarkLogo) {
        this.watermarkLogo = watermarkLogo;
    }

    public float getFilenameFontSize() {
        return filenameFontSize;
    }

    public void setFilenameFontSize(float filenameFontSize) {
        this.filenameFontSize = filenameFontSize;
    }
}
