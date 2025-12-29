## Welcome to the Script Control Panel repository!

More about it, you can find [here](https://github.com/szymciogrosik/script-control-panel/wiki).

## Build

To build the project, run the following command:

```
mvn clean install
```

### Building on Windows

To build the Windows executable (containing the JRE):

1.  Open PowerShell.
2.  Run the build script:
    ```powershell
    ./scripts/build_windows_app.ps1
    ```
3.  The output ZIP file (`ScriptControlPanel-Win.zip`) will be located in the `output` directory.
