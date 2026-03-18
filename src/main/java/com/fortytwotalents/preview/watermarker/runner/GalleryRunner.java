package com.fortytwotalents.preview.watermarker.runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeExceptionMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fortytwotalents.preview.watermarker.config.GalleryProperties;
import com.fortytwotalents.preview.watermarker.service.ImageProcessingService;

/**
 * {@link CommandLineRunner} that drives the gallery processing pipeline.
 *
 * <p>The input and output directories are taken from {@link GalleryProperties}.
 * The runner validates that the input directory exists before delegating to
 * {@link ImageProcessingService}.
 */
@Component
public class GalleryRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GalleryRunner.class);

    private final GalleryProperties properties;
    private final ImageProcessingService imageProcessingService;

    public GalleryRunner(GalleryProperties properties, ImageProcessingService imageProcessingService) {
        this.properties = properties;
        this.imageProcessingService = imageProcessingService;
    }

    @Override
    public void run(String... args) throws Exception {
        Path inputDir = Paths.get(properties.getInputDir()).toAbsolutePath().normalize();
        Path outputDir = Paths.get(properties.getOutputDir()).toAbsolutePath().normalize();

        log.info("Input directory  : {}", inputDir);
        log.info("Output directory : {}", outputDir);
        log.info("Resize factor    : {}", properties.getResizeFactor());
        log.info("Watermark        : {}", properties.getWatermarkPath());

        if (!Files.isDirectory(inputDir)) {
            throw new IllegalArgumentException("Input directory does not exist or is not a directory: " + inputDir);
        }

        Files.createDirectories(outputDir);

        try {
            imageProcessingService.processGallery(inputDir, outputDir);
            log.info("Gallery processing complete.");
        } catch (IOException e) {
            log.error("Gallery processing failed", e);
            throw e;
        }
    }

    @Bean
    public ExitCodeExceptionMapper exitCodeExceptionMapper() {
        return exception -> {
            if (exception.getCause() instanceof IllegalArgumentException) {
                return 2;
            }
            return 1;
        };
    }

}
