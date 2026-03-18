package com.fortytwotalents.preview.watermarker.service;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.fortytwotalents.preview.watermarker.config.GalleryProperties;

/**
 * Service that recursively processes an image gallery by resizing each image,
 * compositing a centred watermark, and burning the original filename into the
 * bottom-left corner before exporting the result as PNG.
 */
@Service
public class ImageProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessingService.class);

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "webp");

    private static final int FILENAME_FONT_SIZE = 14;
    private static final int FILENAME_PADDING = 8;

    private final GalleryProperties properties;
    private final ResourceLoader resourceLoader;

    public ImageProcessingService(GalleryProperties properties) {
        this.properties = properties;
        this.resourceLoader = new DefaultResourceLoader();
    }

    /**
     * Processes all supported images found recursively under {@code inputDir},
     * writing the results under {@code outputDir} while preserving the sub-directory structure.
     *
     * @param inputDir  root of the source image gallery
     * @param outputDir root of the destination directory
     * @throws IOException if an I/O error occurs
     */
    public void processGallery(Path inputDir, Path outputDir) throws IOException {
        BufferedImage watermark = loadWatermark();

        try (var stream = Files.walk(inputDir)) {
            stream.filter(Files::isRegularFile)
                  .filter(this::isSupportedImage)
                  .forEach(source -> {
                      Path relative = inputDir.relativize(source);
                      Path destination = outputDir.resolve(relative)
                                                  .resolveSibling(toOutputFilename(source.getFileName().toString()));
                      try {
                          Files.createDirectories(destination.getParent());
                          processImage(source, destination, watermark);
                      } catch (IOException e) {
                          log.error("Failed to process image: {}", source, e);
                      }
                  });
        }
    }

    /**
     * Processes a single image file: resizes it, composites the watermark centred on the
     * canvas, burns the original filename into the bottom-left corner, and writes the
     * result as PNG to {@code destination}.
     *
     * @param source      path to the source image
     * @param destination path where the PNG output will be written
     * @param watermark   the watermark image, or {@code null} if unavailable
     * @throws IOException if an I/O error occurs
     */
    public void processImage(Path source, Path destination, BufferedImage watermark) throws IOException {
        log.info("Processing: {} -> {}", source, destination);

        BufferedImage original = ImageIO.read(source.toFile());
        if (original == null) {
            log.warn("Skipping unreadable image: {}", source);
            return;
        }

        BufferedImage resized = resizeImage(original, properties.getResizeFactor());

        if (watermark != null) {
            applyWatermark(resized, watermark);
        }

        burnFilename(resized, source.getFileName().toString());

        ImageIO.write(resized, "PNG", destination.toFile());
        log.info("Written: {}", destination);
    }

    /**
     * Scales {@code source} by {@code factor} using bicubic interpolation.
     *
     * @param source the original image
     * @param factor scale factor (e.g. {@code 0.5} halves both dimensions)
     * @return a new {@link BufferedImage} at the scaled dimensions
     */
    public BufferedImage resizeImage(BufferedImage source, double factor) {
        int newWidth = Math.max(1, (int) Math.round(source.getWidth() * factor));
        int newHeight = Math.max(1, (int) Math.round(source.getHeight() * factor));

        BufferedImage output = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = output.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(source, 0, 0, newWidth, newHeight, null);
        } finally {
            g2d.dispose();
        }
        return output;
    }

    /**
     * Composites {@code watermark} centred on {@code canvas} using the watermark's own
     * alpha channel so that it appears as a semi-transparent overlay.
     *
     * @param canvas    the image to draw the watermark onto (modified in place)
     * @param watermark the watermark image
     */
    public void applyWatermark(BufferedImage canvas, BufferedImage watermark) {
        int x = (canvas.getWidth() - watermark.getWidth()) / 2;
        int y = (canvas.getHeight() - watermark.getHeight()) / 2;

        Graphics2D g2d = canvas.createGraphics();
        try {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g2d.drawImage(watermark, x, y, null);
        } finally {
            g2d.dispose();
        }
    }

    /**
     * Renders {@code filename} in the bottom-left corner of {@code canvas} with a
     * semi-transparent dark background rectangle for readability.
     *
     * @param canvas   the image to annotate (modified in place)
     * @param filename the text to render
     */
    public void burnFilename(BufferedImage canvas, String filename) {
        Graphics2D g2d = canvas.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            Font font = new Font(Font.SANS_SERIF, Font.PLAIN, FILENAME_FONT_SIZE);
            g2d.setFont(font);
            FontMetrics metrics = g2d.getFontMetrics(font);

            int textWidth = metrics.stringWidth(filename);
            int textHeight = metrics.getHeight();

            int bgX = FILENAME_PADDING;
            int bgY = canvas.getHeight() - textHeight - FILENAME_PADDING * 2;
            int bgWidth = textWidth + FILENAME_PADDING * 2;
            int bgHeight = textHeight + FILENAME_PADDING;

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            g2d.setColor(Color.BLACK);
            g2d.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 4, 4);

            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2d.setColor(Color.WHITE);
            g2d.drawString(filename, bgX + FILENAME_PADDING, bgY + metrics.getAscent() + FILENAME_PADDING / 2);
        } finally {
            g2d.dispose();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private BufferedImage loadWatermark() {
        String path = properties.getWatermarkPath();
        try {
            var resource = resourceLoader.getResource(path);
            if (!resource.exists()) {
                log.warn("Watermark not found at '{}'; watermark will be skipped.", path);
                return null;
            }
            try (InputStream is = resource.getInputStream()) {
                BufferedImage wm = ImageIO.read(is);
                if (wm == null) {
                    log.warn("Could not decode watermark image at '{}'; watermark will be skipped.", path);
                }
                return wm;
            }
        } catch (IOException e) {
            log.warn("Error loading watermark '{}'; watermark will be skipped.", path, e);
            return null;
        }
    }

    private boolean isSupportedImage(Path path) {
        String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        return SUPPORTED_EXTENSIONS.contains(name.substring(dot + 1));
    }

    private String toOutputFilename(String originalName) {
        int dot = originalName.lastIndexOf('.');
        String base = dot >= 0 ? originalName.substring(0, dot) : originalName;
        return base + ".png";
    }

}
