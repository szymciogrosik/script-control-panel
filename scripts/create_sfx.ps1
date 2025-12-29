param (
    [string]$AppImageDir = "output\ScriptControlPanel",
    [string]$OutputFile = "output\ScriptControlPanel.exe"
)

$7zPath = "7z"
$SfxModule = "7zsd.sfx"

# Check if 7z is available
if (-not (Get-Command $7zPath -ErrorAction SilentlyContinue)) {
    Write-Error "7z not found in PATH. Please install 7-Zip."
    exit 1
}

# Create Config for SFX
$ConfigContent = ";!@Install@!UTF-8!
Title=\"Script Control Panel\"
RunProgram=\"ScriptControlPanel\ScriptControlPanel.exe\"
GUIMode=\"2\"
;!@InstallEnd@!"

$ConfigContent | Out-File -FilePath "sfx_config.txt" -Encoding UTF8

echo "Archiving app image..."
& $7zPath a "app.7z" "$AppImageDir"

echo "Creating SFX..."
# Binary concatenation: 7zsd.sfx + config + archive -> exe
# Note: We need the 7zsd.sfx module.
# On GH Actions, we will download it. Locally, user needs to provide it or we download it.

if (-not (Test-Path $SfxModule)) {
    echo "SFX module '$SfxModule' not found. Attempting to download..."
    # Downloading from a reliable source (7-zip.org) or assume it is in the tools folder
    # For now, let's assume the user/CI provides it.
    Write-Error "SFX Module $SfxModule is missing."
    exit 1
}

cmd /c "copy /b $SfxModule + sfx_config.txt + app.7z $OutputFile"

echo "Cleanup..."
Remove-Item "sfx_config.txt"
Remove-Item "app.7z"

echo "Done. Single EXE created at $OutputFile"
