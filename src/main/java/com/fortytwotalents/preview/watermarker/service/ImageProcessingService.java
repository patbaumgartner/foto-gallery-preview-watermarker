package com.fortytwotalents.preview.watermarker.service;

import com.fortytwotalents.preview.watermarker.config.WatermarkerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Service responsible for walking the input folder tree, processing each image
 * (resize, watermark, filename overlay) and writing results as PNG files into
 * the output folder while preserving the directory structure.
 */
@Service
public class ImageProcessingService {

    private static final Logger log = LoggerFactory.getLogger(ImageProcessingService.class);

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "bmp", "tiff", "tif", "webp");

    private final WatermarkerProperties properties;
    private final ResourceLoader resourceLoader = new DefaultResourceLoader();

    public ImageProcessingService(WatermarkerProperties properties) {
        this.properties = properties;
    }

    /**
     * Processes all images found under {@code inputFolder}, writing resized and
     * watermarked PNG copies to {@code outputFolder} while mirroring the sub-folder
     * structure.
     *
     * @throws IOException if an I/O error occurs
     */
    public void process() throws IOException {
        Path inputRoot = Paths.get(properties.getInputFolder()).toAbsolutePath().normalize();
        Path outputRoot = Paths.get(properties.getOutputFolder()).toAbsolutePath().normalize();

        if (!Files.isDirectory(inputRoot)) {
            throw new IllegalArgumentException("Input folder does not exist or is not a directory: " + inputRoot);
        }

        BufferedImage watermarkImage = loadWatermark();

        log.info("Processing images from '{}' → '{}'", inputRoot, outputRoot);
        log.info("Resize factor: {}, Watermark: {}", properties.getResizeFactor(), properties.getWatermarkLogo());

        try (Stream<Path> paths = Files.walk(inputRoot)) {
            paths.filter(Files::isRegularFile)
                 .filter(this::isSupportedImage)
                 .forEach(inputFile -> {
                     try {
                         processImage(inputFile, inputRoot, outputRoot, watermarkImage);
                     } catch (IOException e) {
                         log.error("Failed to process image '{}': {}", inputFile, e.getMessage(), e);
                     }
                 });
        }

        log.info("Processing complete.");
    }

    /**
     * Loads the watermark image from the configured location.
     * Falls back to a generated white transparent 128×128 PNG if the resource cannot be found.
     */
    public BufferedImage loadWatermark() throws IOException {
        String logoPath = properties.getWatermarkLogo();
        Resource resource = resourceLoader.getResource(logoPath);
        if (resource.exists()) {
            try (InputStream is = resource.getInputStream()) {
                BufferedImage logo = ImageIO.read(is);
                if (logo != null) {
                    log.info("Loaded watermark from '{}'", logoPath);
                    return ensureTransparency(logo);
                }
            }
        }

        // Check if the path refers to a plain file path
        Path logoFilePath = Paths.get(logoPath);
        if (Files.isReadable(logoFilePath)) {
            BufferedImage logo = ImageIO.read(logoFilePath.toFile());
            if (logo != null) {
                log.info("Loaded watermark from file '{}'", logoFilePath);
                return ensureTransparency(logo);
            }
        }

        log.warn("Watermark logo not found at '{}', generating a default white transparent logo.", logoPath);
        return generateDefaultWatermark();
    }

    /**
     * Ensures the image has an alpha channel (ARGB). If it is already ARGB the
     * original instance is returned unchanged.
     */
    private BufferedImage ensureTransparency(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
            return src;
        }
        BufferedImage argb = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = argb.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return argb;
    }

    /**
     * Generates a simple white semi-transparent watermark as a fallback when no
     * {@code logo.png} is configured or found.
     */
    private BufferedImage generateDefaultWatermark() {
        int size = 128;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // White semi-transparent circle
        g.setColor(new Color(255, 255, 255, 180));
        g.fillOval(4, 4, size - 8, size - 8);
        g.setColor(new Color(255, 255, 255, 220));
        g.setStroke(new BasicStroke(3f));
        g.drawOval(4, 4, size - 8, size - 8);
        g.dispose();
        return img;
    }

    /**
     * Returns {@code true} if the given path has a file extension that corresponds
     * to a supported image format.
     */
    private boolean isSupportedImage(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            return false;
        }
        return SUPPORTED_EXTENSIONS.contains(name.substring(dot + 1));
    }

    /**
     * Processes a single image: resize → watermark → filename overlay → export PNG.
     */
    private void processImage(Path inputFile, Path inputRoot, Path outputRoot, BufferedImage watermarkImage)
            throws IOException {

        BufferedImage original = ImageIO.read(inputFile.toFile());
        if (original == null) {
            log.warn("Could not read image '{}', skipping.", inputFile);
            return;
        }

        // 1. Resize
        double factor = properties.getResizeFactor();
        int newWidth = Math.max(1, (int) (original.getWidth() / factor));
        int newHeight = Math.max(1, (int) (original.getHeight() / factor));
        BufferedImage resized = resize(original, newWidth, newHeight);

        // 2. Composite watermark (centred, scaled to fit within the resized image)
        BufferedImage watermarked = applyWatermark(resized, watermarkImage);

        // 3. Render filename
        String filename = inputFile.getFileName().toString();
        BufferedImage result = renderFilename(watermarked, filename);

        // 4. Determine output path (same relative path, always .png extension)
        Path relative = inputRoot.relativize(inputFile);
        String outputFilename = stripExtension(filename) + ".png";
        Path outputFile = outputRoot.resolve(relative).resolveSibling(outputFilename);

        Files.createDirectories(outputFile.getParent());
        ImageIO.write(result, "png", outputFile.toFile());
        log.info("Written: {}", outputFile);
    }

    /**
     * Resizes {@code src} to the given dimensions using bicubic interpolation.
     */
    public BufferedImage resize(BufferedImage src, int width, int height) {
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dest.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return dest;
    }

    /**
     * Composites the watermark image centred on the base image.
     * The watermark is scaled down proportionally so it fits within 50% of the
     * base image's shortest side.
     */
    public BufferedImage applyWatermark(BufferedImage base, BufferedImage watermark) {
        int baseW = base.getWidth();
        int baseH = base.getHeight();

        // Scale watermark to at most 50% of the shortest base dimension
        int maxWM = Math.max(1, Math.min(baseW, baseH) / 2);
        int wmW = watermark.getWidth();
        int wmH = watermark.getHeight();
        if (wmW > maxWM || wmH > maxWM) {
            double scale = (double) maxWM / Math.max(wmW, wmH);
            wmW = Math.max(1, (int) (wmW * scale));
            wmH = Math.max(1, (int) (wmH * scale));
        }

        int x = (baseW - wmW) / 2;
        int y = (baseH - wmH) / 2;

        BufferedImage result = new BufferedImage(baseW, baseH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(base, 0, 0, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        g.drawImage(watermark, x, y, wmW, wmH, null);
        g.dispose();
        return result;
    }

    /**
     * Renders the image's original filename as white text with a dark drop-shadow
     * in the bottom-left corner of the image.
     */
    public BufferedImage renderFilename(BufferedImage base, String filename) {
        BufferedImage result = new BufferedImage(base.getWidth(), base.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.drawImage(base, 0, 0, null);

        Font font = new Font(Font.SANS_SERIF, Font.BOLD, Math.round(properties.getFilenameFontSize()));
        g.setFont(font);

        int padding = 4;
        int textX = padding;
        int textY = base.getHeight() - padding;

        // Shadow
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(filename, textX + 1, textY + 1);
        // White text
        g.setColor(Color.WHITE);
        g.drawString(filename, textX, textY);

        g.dispose();
        return result;
    }

    /**
     * Removes the file extension from {@code filename}.
     */
    private String stripExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(0, dot) : filename;
    }
}
