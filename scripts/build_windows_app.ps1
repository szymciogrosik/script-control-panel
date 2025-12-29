# build_windows_app.ps1
# Builds the project using Maven and then packages it using jpackage for Windows.

# Stop on first error
$ErrorActionPreference = "Stop"

# 1. Build with Maven
Write-Host "Building with Maven..."
mvn clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven build failed."
    exit 1
}

# 2. Prepare for jpackage
$targetDir = "target"
$jpackageInputDir = "$targetDir/jpackage-input"
$outputDir = "output"
$jarName = "script-control-panel.jar"

Write-Host "Preparing directories..."
if (Test-Path $outputDir) {
    Remove-Item -Recurse -Force $outputDir
}
if (Test-Path $jpackageInputDir) {
    Remove-Item -Recurse -Force $jpackageInputDir
}

New-Item -ItemType Directory -Force -Path $jpackageInputDir | Out-Null
New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

Write-Host "Copying jar..."
Copy-Item "$targetDir/$jarName" -Destination "$jpackageInputDir/$jarName"

# 3. Run jpackage
Write-Host "Running jpackage..."
# Note: This command assumes running on Windows where jpackage creates Windows artifacts.
jpackage `
  --type app-image `
  --input $jpackageInputDir `
  --main-jar $jarName `
  --dest $outputDir `
  --name "ScriptControlPanel" `
  --verbose

if ($LASTEXITCODE -ne 0) {
    Write-Error "jpackage failed."
    exit 1
}

# 4. Zip the output
$appName = "ScriptControlPanel"
$zipName = "ScriptControlPanel-Win.zip"
$sourcePath = Join-Path $outputDir $appName
$zipPath = Join-Path $outputDir $zipName

Write-Host "Zipping output..."
if (Test-Path $sourcePath) {
    Compress-Archive -Path $sourcePath -DestinationPath $zipPath -Force
    Write-Host "Build success. Output zipped at $zipPath"
} else {
    Write-Error "Output directory not found. jpackage might have failed."
    exit 1
}
