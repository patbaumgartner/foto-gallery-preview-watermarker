package com.fortytwotalents.preview.watermarker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.fortytwotalents.preview.watermarker.config.GalleryProperties;

@SpringBootApplication
@EnableConfigurationProperties(GalleryProperties.class)
public class FotoGalleryPreviewWatermarkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FotoGalleryPreviewWatermarkerApplication.class, args);
    }

}
