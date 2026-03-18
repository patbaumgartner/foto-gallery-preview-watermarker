@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION
REM ----------------------------------------------------------------------------
REM Foto Gallery Preview Watermarker — convenience launcher (Windows)
REM
REM Usage (positional):
REM   watermark.bat [INPUT_DIR] [OUTPUT_DIR] [RESIZE_FACTOR] [EXTRA_ARGS...]
REM
REM Usage (flag-based):
REM   watermark.bat --gallery.input-dir=photos --gallery.output-dir=out
REM
REM Defaults:
REM   INPUT_DIR     = input
REM   OUTPUT_DIR    = output
REM   RESIZE_FACTOR = 0.5
REM ----------------------------------------------------------------------------

SET SCRIPT_DIR=%~dp0
SET SCRIPT_DIR=%SCRIPT_DIR:~0,-1%

REM Prefer native binary over JAR when available
IF EXIST "%SCRIPT_DIR%\foto-gallery-preview-watermarker.exe" (
  SET RUNNER="%SCRIPT_DIR%\foto-gallery-preview-watermarker.exe"
  GOTO RUN
)
IF EXIST "%SCRIPT_DIR%\target\foto-gallery-preview-watermarker.exe" (
  SET RUNNER="%SCRIPT_DIR%\target\foto-gallery-preview-watermarker.exe"
  GOTO RUN
)

REM Fall back to JAR
FOR /F "delims=" %%J IN ('dir /b /s "%SCRIPT_DIR%\target\*.jar" ^| findstr /V "original" 2^>NUL') DO (
  SET JAR=%%J
  GOTO FOUND_JAR
)
ECHO No JAR or native binary found. Build the project first: 1>&2
ECHO   mvnw.cmd clean package -DskipTests 1>&2
EXIT /B 1

:FOUND_JAR
SET RUNNER=java -jar "!JAR!"

:RUN
REM Detect whether arguments use --flag style or positional style
SET FIRST_ARG=%~1
IF "%FIRST_ARG:~0,2%"=="--" (
  REM Flag-based — pass everything directly
  %RUNNER% %*
) ELSE (
  REM Positional style
  SET INPUT_DIR=%~1
  IF "!INPUT_DIR!"=="" SET INPUT_DIR=input

  SET OUTPUT_DIR=%~2
  IF "!OUTPUT_DIR!"=="" SET OUTPUT_DIR=output

  SET RESIZE_FACTOR=%~3
  IF "!RESIZE_FACTOR!"=="" SET RESIZE_FACTOR=0.5

  %RUNNER% "--gallery.input-dir=!INPUT_DIR!" "--gallery.output-dir=!OUTPUT_DIR!" "--gallery.resize-factor=!RESIZE_FACTOR!"
)

ENDLOCAL
