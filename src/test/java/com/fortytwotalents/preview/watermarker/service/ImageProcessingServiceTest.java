package com.fortytwotalents.preview.watermarker.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fortytwotalents.preview.watermarker.config.GalleryProperties;

class ImageProcessingServiceTest {

    private ImageProcessingService service;

    @BeforeEach
    void setUpService() {
        GalleryProperties props = new GalleryProperties();
        props.setResizeFactor(0.5);
        props.setWatermarkPath("classpath:watermark.png");
        service = new ImageProcessingService(props);
    }

    @TempDir
    Path tempDir;

    private BufferedImage sampleImage;
    private BufferedImage watermarkImage;

    @BeforeEach
    void setUp() {
        // 200x100 red rectangle (RGB so it can be written as JPEG in tests)
        sampleImage = new BufferedImage(200, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sampleImage.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 200, 100);
        g.dispose();

        // 60x20 white-transparent watermark
        watermarkImage = new BufferedImage(60, 20, BufferedImage.TYPE_INT_ARGB);
    }

    // -------------------------------------------------------------------------
    // resizeImage
    // -------------------------------------------------------------------------

    @Test
    void resizeImage_halvesDimensions() {
        BufferedImage result = service.resizeImage(sampleImage, 0.5);

        assertThat(result.getWidth()).isEqualTo(100);
        assertThat(result.getHeight()).isEqualTo(50);
    }

    @Test
    void resizeImage_keepsOriginalDimensionsForFactorOne() {
        BufferedImage result = service.resizeImage(sampleImage, 1.0);

        assertThat(result.getWidth()).isEqualTo(200);
        assertThat(result.getHeight()).isEqualTo(100);
    }

    @Test
    void resizeImage_doublesDimensions() {
        BufferedImage result = service.resizeImage(sampleImage, 2.0);

        assertThat(result.getWidth()).isEqualTo(400);
        assertThat(result.getHeight()).isEqualTo(200);
    }

    @Test
    void resizeImage_returnsArgbImage() {
        BufferedImage result = service.resizeImage(sampleImage, 0.5);

        assertThat(result.getType()).isEqualTo(BufferedImage.TYPE_INT_ARGB);
    }

    // -------------------------------------------------------------------------
    // applyWatermark
    // -------------------------------------------------------------------------

    @Test
    void applyWatermark_doesNotChangeDimensions() {
        BufferedImage canvas = service.resizeImage(sampleImage, 1.0);
        int originalWidth = canvas.getWidth();
        int originalHeight = canvas.getHeight();

        service.applyWatermark(canvas, watermarkImage);

        assertThat(canvas.getWidth()).isEqualTo(originalWidth);
        assertThat(canvas.getHeight()).isEqualTo(originalHeight);
    }

    // -------------------------------------------------------------------------
    // burnFilename
    // -------------------------------------------------------------------------

    @Test
    void burnFilename_doesNotChangeDimensions() {
        BufferedImage canvas = service.resizeImage(sampleImage, 1.0);
        int originalWidth = canvas.getWidth();
        int originalHeight = canvas.getHeight();

        service.burnFilename(canvas, "test-image.jpg");

        assertThat(canvas.getWidth()).isEqualTo(originalWidth);
        assertThat(canvas.getHeight()).isEqualTo(originalHeight);
    }

    // -------------------------------------------------------------------------
    // processImage
    // -------------------------------------------------------------------------

    @Test
    void processImage_writesPngFile(@TempDir Path tmp) throws IOException {
        Path source = tmp.resolve("photo.jpg");
        ImageIO.write(sampleImage, "JPEG", source.toFile());

        Path destination = tmp.resolve("photo.png");
        service.processImage(source, destination, watermarkImage);

        assertThat(destination).exists();
        BufferedImage output = ImageIO.read(destination.toFile());
        assertThat(output).isNotNull();
        // resize factor 0.5 → 100×50
        assertThat(output.getWidth()).isEqualTo(100);
        assertThat(output.getHeight()).isEqualTo(50);
    }

    @Test
    void processImage_worksWithoutWatermark(@TempDir Path tmp) throws IOException {
        Path source = tmp.resolve("photo.png");
        ImageIO.write(sampleImage, "PNG", source.toFile());

        Path destination = tmp.resolve("photo-out.png");
        service.processImage(source, destination, null);

        assertThat(destination).exists();
    }

    // -------------------------------------------------------------------------
    // processGallery – directory structure preserved
    // -------------------------------------------------------------------------

    @Test
    void processGallery_preservesDirectoryStructure(@TempDir Path tmp) throws IOException {
        // Input layout:
        //   tmp/input/
        //     a.jpg
        //     sub/
        //       b.png
        Path inputDir = tmp.resolve("input");
        Path subDir = inputDir.resolve("sub");
        Files.createDirectories(subDir);

        ImageIO.write(sampleImage, "JPEG", inputDir.resolve("a.jpg").toFile());
        ImageIO.write(sampleImage, "PNG", subDir.resolve("b.png").toFile());

        Path outputDir = tmp.resolve("output");

        service.processGallery(inputDir, outputDir);

        assertThat(outputDir.resolve("a.png")).exists();
        assertThat(outputDir.resolve("sub").resolve("b.png")).exists();
    }

    @Test
    void processGallery_skipsNonImageFiles(@TempDir Path tmp) throws IOException {
        Path inputDir = tmp.resolve("input");
        Files.createDirectories(inputDir);
        Files.writeString(inputDir.resolve("notes.txt"), "not an image");

        Path outputDir = tmp.resolve("output");

        service.processGallery(inputDir, outputDir);

        // output dir is created (mirrors inputDir) but contains no image files
        assertThat(outputDir).exists();
        assertThat(Files.list(outputDir).count()).isZero();
    }

    @Test
    void processGallery_mirrorsEmptySubdirectories(@TempDir Path tmp) throws IOException {
        // Input layout:
        //   tmp/input/
        //     emptySubDir/
        //     a.jpg
        Path inputDir = tmp.resolve("input");
        Path emptySubDir = inputDir.resolve("emptySubDir");
        Files.createDirectories(emptySubDir);
        ImageIO.write(sampleImage, "JPEG", inputDir.resolve("a.jpg").toFile());

        Path outputDir = tmp.resolve("output");

        service.processGallery(inputDir, outputDir);

        assertThat(outputDir.resolve("emptySubDir")).isDirectory();
        assertThat(outputDir.resolve("a.png")).exists();
    }

}
