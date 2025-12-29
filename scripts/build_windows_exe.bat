@echo off
setlocal

echo Cleaning up previous build...
rmdir /s /q output 2>nul

echo Building Windows Executable (App Image)...
jpackage ^
  --type app-image ^
  --input target/libs ^
  --main-jar script-control-panel.jar ^
  --main-class org.codefromheaven.App ^
  --name "ScriptControlPanel" ^
  --dest output ^
  --icon src/main/resources/icons/icon.ico ^
  --java-options "-Dfile.encoding=UTF-8"

if %errorlevel% neq 0 (
    echo Build failed!
    exit /b %errorlevel%
)

echo Build complete. The application is located in 'output\ScriptControlPanel'.
