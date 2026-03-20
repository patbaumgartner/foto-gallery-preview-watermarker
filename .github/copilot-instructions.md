# GitHub Copilot Instructions

## Project Overview

**Foto Gallery Preview Watermarker** is a Spring Boot command-line tool that recursively processes a photo gallery: it resizes images, composites a watermark at the centre, and burns the original filename into the bottom-left corner of every image — all while preserving the original directory structure and converting outputs to PNG.

---

## Technology Stack

| Component    | Version / Detail                                  |
|--------------|---------------------------------------------------|
| Java         | 25 (OpenJDK / Temurin)                            |
| Spring Boot  | 4.0.3 (console / `CommandLineRunner` application) |
| Maven        | 3.9+ (Maven wrapper `./mvnw` included)            |
| Image I/O    | `java.awt` / `javax.imageio` — no extra image libs|
| Architecture | Taikai (ArchUnit wrapper) enforced in tests        |
| Formatting   | Spring Java Format                                |

---

## Package Structure

```
com.fortytwotalents.preview.watermarker
├── FotoGalleryPreviewWatermarkerApplication   # @SpringBootApplication entry point
├── config
│   └── GalleryProperties                     # @ConfigurationProperties (prefix = "gallery")
├── runner
│   └── GalleryRunner                         # CommandLineRunner — orchestrates the pipeline
└── service
    └── ImageProcessingService                # Core image-processing logic
```

---

## Key Classes

### `GalleryProperties`

Holds all user-configurable settings bound from `application.properties`, environment variables, or CLI flags (prefix `gallery`):

| Property          | Default                  | Description                                          |
|-------------------|--------------------------|------------------------------------------------------|
| `inputDir`        | `input`                  | Source directory containing the image gallery        |
| `outputDir`       | `output`                 | Destination directory for processed images           |
| `resizeFactor`    | `0.5`                    | Scale factor for width & height (`0.5` = half size)  |
| `watermarkPath`   | `classpath:watermark.png`| Path to the watermark PNG (classpath/file/http)      |

### `ImageProcessingService`

The heart of the application. Key public methods:

- `processGallery(Path inputDir, Path outputDir)` — walks the tree, mirrors the directory structure, and processes each supported image.
- `processImage(Path source, Path destination, BufferedImage watermark)` — resizes, applies watermark, burns filename, writes PNG.
- `resizeImage(BufferedImage source, double factor)` — bicubic interpolation with anti-aliasing.
- `applyWatermark(BufferedImage canvas, BufferedImage watermark)` — centres the watermark using `SRC_OVER` alpha compositing.
- `burnFilename(BufferedImage canvas, String filename)` — renders the filename with a semi-transparent background in the bottom-left corner.

Supported input formats: `JPG`, `JPEG`, `PNG`, `GIF`, `BMP`, `TIFF`, `TIF`, `WEBP`. Output is always **PNG**.

---

## Coding Conventions

- **Constructor injection** — never use `@Autowired` field injection.
- **Spring Java Format** — apply before committing: `./mvnw validate -Pcode-cleanup`.
- **Taikai architecture rules** (enforced in `ArchitectureTest`):
  - No `@Autowired` fields anywhere.
  - Classes annotated `@Configuration` must be named `*Configuration`.
  - Classes annotated `@Service` must be named `*Service`.
  - Classes annotated `@Repository` must be named `*Repository`.
  - No import cycles; no `sun..` / `com.sun..` imports; no direct use of `Thread`.
- **Conventional Commits** for all commit messages: `feat`, `fix`, `docs`, `chore`, `refactor`, `test`, `ci`.
- Place unit tests under `src/test/java` mirroring the main package structure.

---

## Build & Test Commands

```bash
# Build and run all tests (including coverage via JaCoCo)
./mvnw clean verify

# Build fat JAR only (skip tests)
./mvnw clean package -DskipTests

# Build GraalVM native image (requires GraalVM 25+)
./mvnw clean package -Pnative -DskipTests

# Apply Spring Java Format + SortPom + OpenRewrite + dependency updates
./mvnw validate -Pcode-cleanup
```

> **Note:** A Maven wrapper is included. You only need Java 25 on your `PATH`.

---

## Configuration Examples

**`application.properties`:**

```properties
gallery.input-dir=photos
gallery.output-dir=previews
gallery.resize-factor=0.75
gallery.watermark-path=file:logo.png
```

**CLI flags:**

```bash
java -jar foto-gallery-preview-watermarker-*.jar \
  --gallery.input-dir=photos \
  --gallery.output-dir=previews \
  --gallery.resize-factor=0.75
```

---

## Testing Practices

- Tests live in `src/test/java` and use **JUnit 5** via `spring-boot-starter-test`.
- `ImageProcessingServiceTest` covers the core image-processing methods with real `BufferedImage` instances (no mocks for image data).
- `ArchitectureTest` uses **Taikai** to enforce architectural and naming rules at build time.
- JaCoCo generates a coverage report at `target/site/jacoco/` during `mvn verify`.
- When adding a new service, also add a corresponding `*Test` class and ensure it passes the Taikai naming rules.

---

## CI / CD

- **CI workflow** (`.github/workflows/ci.yml`): runs `./mvnw -B verify` on every push and pull request to `main` using Java 25 (Temurin).
- **Release workflow** (`.github/workflows/release.yml`): builds and publishes release artifacts (fat JAR + native binaries for Linux and Windows).
