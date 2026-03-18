package com.fortytwotalents.preview.watermarker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the foto-gallery-preview-watermarker.
 *
 * <p>All properties are prefixed with {@code gallery} in {@code application.properties}.
 */
@ConfigurationProperties(prefix = "gallery")
public class GalleryProperties {

    /** Path to the input directory containing the image gallery. */
    private String inputDir = "input";

    /** Path to the output directory where processed images will be written. */
    private String outputDir = "output";

    /**
     * Resize factor applied to both width and height of each image.
     * A value of {@code 0.5} halves the dimensions; {@code 1.0} keeps them unchanged.
     */
    private double resizeFactor = 0.5;

    /** Path to the white-transparent PNG watermark image. */
    private String watermarkPath = "classpath:watermark.png";

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public double getResizeFactor() {
        return resizeFactor;
    }

    public void setResizeFactor(double resizeFactor) {
        this.resizeFactor = resizeFactor;
    }

    public String getWatermarkPath() {
        return watermarkPath;
    }

    public void setWatermarkPath(String watermarkPath) {
        this.watermarkPath = watermarkPath;
    }

}
