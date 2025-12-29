# How to Build Windows Executable

This guide explains how to build the **Single Monolithic Executable (SFX)** for the Script Control Panel on Windows.

## Prerequisites

1.  **Java JDK 17+**: Ensure you have JDK 17 or later installed.
2.  **Maven**: Ensure Maven is installed and available on your PATH.
3.  **7-Zip**: You must have 7-Zip installed (typically `C:\Program Files\7-Zip`).
4.  **7-Zip SFX Module (`7zsd.sfx`)**: You need the "7-Zip Extra" package.
    *   Download from [7-zip.org](https://www.7-zip.org/download.html) (look for "7-Zip Extra").
    *   Extract `7zsd.sfx` from the archive and place it in the root of this project (or update the script to point to it).

## Steps

1.  **Open PowerShell** and navigate to the project root directory.

2.  **Build the Project with Maven**:
    ```powershell
    mvn clean install -DskipTests
    ```

3.  **Build the App Image**:
    Run the batch script to create the raw application folder (bundled with Java):
    ```cmd
    scripts\build_windows_exe.bat
    ```

4.  **Create the Single EXE**:
    Run the PowerShell script to package the folder into a single SFX executable:
    ```powershell
    ./scripts/create_sfx.ps1
    ```
    *Note: This script assumes `7z` is in your PATH or standard location, and `7zsd.sfx` is in the project root.*

5.  **Locate the Output**:
    The final executable will be at `output\ScriptControlPanel.exe`.

## Troubleshooting

*   **"7z not found"**: Add 7-Zip to your PATH or edit `create_sfx.ps1` to point to `7z.exe`.
*   **"SFX module not found"**: Ensure `7zsd.sfx` is in the current directory.
