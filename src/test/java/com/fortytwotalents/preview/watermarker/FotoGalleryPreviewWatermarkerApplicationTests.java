package com.fortytwotalents.preview.watermarker;

import com.fortytwotalents.preview.watermarker.config.WatermarkerProperties;
import com.fortytwotalents.preview.watermarker.service.ImageProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FotoGalleryPreviewWatermarkerApplicationTests {

    @Autowired
    private ImageProcessingService imageProcessingService;

    @Autowired
    private WatermarkerProperties properties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        properties.setResizeFactor(2.0);
        properties.setFilenameFontSize(10.0f);
    }

    @Test
    void contextLoads() {
        assertThat(imageProcessingService).isNotNull();
        assertThat(properties).isNotNull();
    }

    @Test
    void resizeReducesDimensions() {
        BufferedImage original = new BufferedImage(200, 100, BufferedImage.TYPE_INT_ARGB);
        BufferedImage resized = imageProcessingService.resize(original, 100, 50);

        assertThat(resized.getWidth()).isEqualTo(100);
        assertThat(resized.getHeight()).isEqualTo(50);
    }

    @Test
    void resizePreservesAspectRatioWhenCalledCorrectly() {
        BufferedImage original = new BufferedImage(400, 200, BufferedImage.TYPE_INT_ARGB);
        double factor = 4.0;
        int newW = (int) (original.getWidth() / factor);
        int newH = (int) (original.getHeight() / factor);

        BufferedImage resized = imageProcessingService.resize(original, newW, newH);

        assertThat(resized.getWidth()).isEqualTo(100);
        assertThat(resized.getHeight()).isEqualTo(50);
    }

    @Test
    void applyWatermarkRetainsDimensions() throws IOException {
        BufferedImage base = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        BufferedImage watermark = imageProcessingService.loadWatermark();

        BufferedImage result = imageProcessingService.applyWatermark(base, watermark);

        assertThat(result.getWidth()).isEqualTo(300);
        assertThat(result.getHeight()).isEqualTo(200);
    }

    @Test
    void renderFilenameRetainsDimensions() {
        BufferedImage base = new BufferedImage(150, 100, BufferedImage.TYPE_INT_ARGB);

        BufferedImage result = imageProcessingService.renderFilename(base, "test-image.png");

        assertThat(result.getWidth()).isEqualTo(150);
        assertThat(result.getHeight()).isEqualTo(100);
    }

    @Test
    void processCreatesOutputImagesWithPngExtension(@TempDir Path input, @TempDir Path output) throws IOException {
        // Create a test JPEG image in a sub-folder
        Path subDir = input.resolve("subdir");
        Files.createDirectories(subDir);
        BufferedImage testImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        File jpegFile = subDir.resolve("photo.jpg").toFile();
        ImageIO.write(testImage, "jpg", jpegFile);

        properties.setInputFolder(input.toString());
        properties.setOutputFolder(output.toString());
        properties.setResizeFactor(2.0);

        imageProcessingService.process();

        Path expectedOutput = output.resolve("subdir").resolve("photo.png");
        assertThat(expectedOutput).exists();

        BufferedImage result = ImageIO.read(expectedOutput.toFile());
        assertThat(result).isNotNull();
        assertThat(result.getWidth()).isEqualTo(100);
        assertThat(result.getHeight()).isEqualTo(100);
    }

    @Test
    void processHandlesNestedSubFolders(@TempDir Path input, @TempDir Path output) throws IOException {
        // Create images in nested directories
        Path level1 = input.resolve("level1");
        Path level2 = level1.resolve("level2");
        Files.createDirectories(level2);

        BufferedImage testImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        ImageIO.write(testImage, "png", level1.resolve("img1.png").toFile());
        ImageIO.write(testImage, "png", level2.resolve("img2.png").toFile());

        properties.setInputFolder(input.toString());
        properties.setOutputFolder(output.toString());
        properties.setResizeFactor(2.0);

        imageProcessingService.process();

        assertThat(output.resolve("level1").resolve("img1.png")).exists();
        assertThat(output.resolve("level1").resolve("level2").resolve("img2.png")).exists();
    }

    @Test
    void loadWatermarkReturnsNonNull() throws IOException {
        BufferedImage watermark = imageProcessingService.loadWatermark();
        assertThat(watermark).isNotNull();
        assertThat(watermark.getWidth()).isGreaterThan(0);
        assertThat(watermark.getHeight()).isGreaterThan(0);
    }
}
