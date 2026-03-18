#!/bin/sh
# ----------------------------------------------------------------------------
# Foto Gallery Preview Watermarker — convenience launcher
#
# Usage (interactive — prompts for each parameter):
#   ./watermark.sh
#
# Usage (positional):
#   ./watermark.sh [INPUT_DIR] [OUTPUT_DIR] [RESIZE_FACTOR] [EXTRA_ARGS...]
#
# Usage (flag-based):
#   ./watermark.sh --gallery.input-dir=photos --gallery.output-dir=out
#
# Defaults:
#   INPUT_DIR     = input
#   OUTPUT_DIR    = output
#   RESIZE_FACTOR = 0.5
# ----------------------------------------------------------------------------
set -euf

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

# Prefer native binary over JAR when available
if [ -f "$SCRIPT_DIR/foto-gallery-preview-watermarker" ]; then
  RUNNER="$SCRIPT_DIR/foto-gallery-preview-watermarker"
elif [ -f "$SCRIPT_DIR/target/foto-gallery-preview-watermarker" ]; then
  RUNNER="$SCRIPT_DIR/target/foto-gallery-preview-watermarker"
else
  JAR=$(find "$SCRIPT_DIR/target" -maxdepth 1 -name "*.jar" -not -name "*.jar.original" 2>/dev/null | head -1)
  if [ -z "$JAR" ]; then
    echo "No JAR or native binary found. Build the project first:" >&2
    echo "  ./mvnw clean package -DskipTests" >&2
    exit 1
  fi
  RUNNER="java -jar $JAR"
fi

# Interactive mode — prompt for each parameter when no arguments are supplied
if [ $# -eq 0 ]; then
  printf "Input directory  [input]:  "
  read -r INPUT_DIR
  printf "Output directory [output]: "
  read -r OUTPUT_DIR
  printf "Resize factor    [0.5]:    "
  read -r RESIZE_FACTOR
  INPUT_DIR="${INPUT_DIR:-input}"
  OUTPUT_DIR="${OUTPUT_DIR:-output}"
  RESIZE_FACTOR="${RESIZE_FACTOR:-0.5}"
  exec $RUNNER \
    "--gallery.input-dir=${INPUT_DIR}" \
    "--gallery.output-dir=${OUTPUT_DIR}" \
    "--gallery.resize-factor=${RESIZE_FACTOR}"
fi

# Detect whether arguments use --flag style or positional style
case "${1-}" in
  --*)
    # Flag-based — pass everything directly
    exec $RUNNER "$@"
    ;;
  *)
    # Positional style
    INPUT_DIR="${1:-input}"
    OUTPUT_DIR="${2:-output}"
    RESIZE_FACTOR="${3:-0.5}"
    shift 3 2>/dev/null || true
    exec $RUNNER \
      "--gallery.input-dir=${INPUT_DIR}" \
      "--gallery.output-dir=${OUTPUT_DIR}" \
      "--gallery.resize-factor=${RESIZE_FACTOR}" \
      "$@"
    ;;
esac
