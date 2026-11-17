@echo off
setlocal
where gradle >nul 2>nul
if errorlevel 1 (
  echo Gradle was not found in PATH.
  echo Install Gradle 8.1.1 or newer and Java 17, then run this file again.
  pause
  exit /b 1
)

gradle build
if errorlevel 1 (
  echo Build failed. Check the Gradle output above.
  pause
  exit /b 1
)

echo.
echo Build complete. Your mod JAR is in build\libs\
pause
