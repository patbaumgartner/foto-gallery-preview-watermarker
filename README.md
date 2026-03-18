# Foto Gallery Preview Watermarker

[![CI](https://github.com/patbaumgartner/foto-gallery-preview-watermarker/actions/workflows/ci.yml/badge.svg)](https://github.com/patbaumgartner/foto-gallery-preview-watermarker/actions/workflows/ci.yml)
[![Release](https://github.com/patbaumgartner/foto-gallery-preview-watermarker/actions/workflows/release.yml/badge.svg)](https://github.com/patbaumgartner/foto-gallery-preview-watermarker/actions/workflows/release.yml)
[![Java](https://img.shields.io/badge/Java-25-blue?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?logo=spring-boot)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A Spring Boot command-line tool that recursively processes a photo gallery: it **resizes** images, **composites a watermark** at the centre, and **burns the original filename** into the bottom-left corner of every image — all while preserving the original directory structure and converting outputs to PNG.

---

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [Download a release](#download-a-release)
  - [Build from source](#build-from-source)
- [Usage](#usage)
  - [Quick start (positional arguments)](#quick-start-positional-arguments)
  - [Flag-based arguments](#flag-based-arguments)
  - [Run the JAR directly](#run-the-jar-directly)
- [Configuration](#configuration)
- [Supported Image Formats](#supported-image-formats)
- [Contributing](#contributing)
- [Code of Conduct](#code-of-conduct)
- [License](#license)

---

## Features

- 🖼️ **Recursive gallery processing** — walks the entire input directory tree and mirrors it in the output directory
- 📏 **Configurable resize factor** — scale images up or down with bicubic interpolation and anti-aliasing
- 💧 **Watermark overlay** — composites a transparent PNG watermark centred on each image
- 🏷️ **Filename burn-in** — renders the original filename into the bottom-left corner with a semi-transparent background
- 📁 **Directory structure preservation** — output mirrors the input hierarchy exactly
- 🚀 **Native image support** — can be compiled to a GraalVM native binary for instant startup and no JVM dependency
- ⚙️ **Spring Boot configuration** — all options are configurable via `application.properties`, environment variables, or command-line flags

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| Java (JDK)  | 25+     |
| Maven       | 3.9+    |

> **Tip:** A Maven wrapper (`./mvnw` / `mvnw.cmd`) is included, so you only need Java installed.

---

## Getting Started

### Download a release

Download the pre-built JAR or native binary for your platform from the [Releases](https://github.com/patbaumgartner/foto-gallery-preview-watermarker/releases) page.

Each release contains:
- `foto-gallery-preview-watermarker-<version>.jar` — runnable fat JAR (requires Java 25)
- `foto-gallery-preview-watermarker-<version>-linux.zip` — native binary + `watermark.sh` for Linux
- `foto-gallery-preview-watermarker-<version>-windows.zip` — native binary + `watermark.bat` for Windows

### Build from source

```bash
# Clone the repository
git clone https://github.com/patbaumgartner/foto-gallery-preview-watermarker.git
cd foto-gallery-preview-watermarker

# Build (skipping tests for speed)
./mvnw clean package -DskipTests

# Build including tests
./mvnw clean verify
```

The fat JAR is produced at `target/foto-gallery-preview-watermarker-*.jar`.

To build a **GraalVM native image**:

```bash
./mvnw clean package -Pnative -DskipTests
```

---

## Usage

### Quick start (positional arguments)

Place your images in an `input/` directory and run:

```bash
# Linux / macOS
./watermark.sh

# Windows
watermark.bat
```

| Position | Argument       | Default  |
|----------|----------------|----------|
| 1        | `INPUT_DIR`    | `input`  |
| 2        | `OUTPUT_DIR`   | `output` |
| 3        | `RESIZE_FACTOR`| `0.5`    |

```bash
# Custom directories and a 75 % resize
./watermark.sh photos previews 0.75
```

### Flag-based arguments

```bash
./watermark.sh \
  --gallery.input-dir=photos \
  --gallery.output-dir=previews \
  --gallery.resize-factor=0.75 \
  --gallery.watermark-path=file:/path/to/logo.png
```

### Run the JAR directly

```bash
java -jar target/foto-gallery-preview-watermarker-*.jar \
  --gallery.input-dir=input \
  --gallery.output-dir=output \
  --gallery.resize-factor=0.5
```

---

## Configuration

All options can be set in `application.properties`, as environment variables, or as command-line flags.

| Property | Default | Description |
|---|---|---|
| `gallery.input-dir` | `input` | Source directory containing the image gallery |
| `gallery.output-dir` | `output` | Destination directory for processed images (created if absent) |
| `gallery.resize-factor` | `0.5` | Scale factor applied to width and height (`0.5` = half size, `1.0` = original) |
| `gallery.watermark-path` | `classpath:watermark.png` | Path to the watermark PNG. Supports `classpath:`, `file:`, and `http:` prefixes |

**Example `application.properties`:**

```properties
gallery.input-dir=photos
gallery.output-dir=previews
gallery.resize-factor=0.75
gallery.watermark-path=file:logo.png
```

---

## Supported Image Formats

The tool reads the following formats as input and writes all output as **PNG**:

`JPG` · `JPEG` · `PNG` · `GIF` · `BMP` · `TIFF` · `TIF` · `WEBP`

---

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to open issues, suggest improvements, and submit pull requests.

---

## Code of Conduct

This project follows the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). By participating you agree to uphold it.

---

## License

This project is licensed under the [MIT License](LICENSE).
