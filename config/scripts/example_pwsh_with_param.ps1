# Check if any parameters are provided
if ($args.Count -eq 0) {
    Write-Output "No parameters provided. Usage: .\example_pwsh_with_param.ps1 <param1> <param2> ..."
    exit 1
}

# Print all provided parameters
Write-Output "Provided parameters: $args"
