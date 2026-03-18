package com.fortytwotalents.preview.watermarker;

import com.fortytwotalents.preview.watermarker.config.WatermarkerProperties;
import com.fortytwotalents.preview.watermarker.service.ImageProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring Boot console application entry point.
 *
 * <p>The application can be configured via {@code application.properties} or
 * command-line arguments:
 *
 * <pre>
 *   java -jar foto-gallery-preview-watermarker.jar \
 *       --watermarker.input-folder=/photos/originals \
 *       --watermarker.output-folder=/photos/previews \
 *       --watermarker.resize-factor=10 \
 *       --watermarker.watermark-logo=/path/to/logo.png
 * </pre>
 */
@SpringBootApplication
@EnableConfigurationProperties(WatermarkerProperties.class)
public class FotoGalleryPreviewWatermarkerApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FotoGalleryPreviewWatermarkerApplication.class);

    private final ImageProcessingService imageProcessingService;

    public FotoGalleryPreviewWatermarkerApplication(ImageProcessingService imageProcessingService) {
        this.imageProcessingService = imageProcessingService;
    }

    public static void main(String[] args) {
        SpringApplication.run(FotoGalleryPreviewWatermarkerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting foto-gallery-preview-watermarker");
        imageProcessingService.process();
    }
}
