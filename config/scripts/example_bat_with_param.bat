@echo off
REM Check if any parameters are provided
if "%1"=="" (
    echo No parameters provided. Usage: example_bat_with_params.bat ^<param1^> ^<param2^> ...
    exit /b 1
)

REM Print all provided parameters
echo Provided parameters: %*
