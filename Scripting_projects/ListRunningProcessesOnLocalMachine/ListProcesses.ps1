param(
    $path = "add your path to the output directory"
)

# Check if path exists, if not create it
if (!(Test-Path -Path $path)) {
    try {
        Write-Host "Path not exist !!" -ForegroundColor Yellow
        New-Item -ItemType Directory -Path $path -Force
        Write-Host "Created directory: $path" -ForegroundColor Yellow
    }
    catch {
        Write-Host "Failed to create directory. Using default path." -ForegroundColor Red
        $path = "E:\ASO\PowerShellComands\output"
        
        # Try to create default path if it doesn't exist
        if (!(Test-Path -Path $path)) {
            New-Item -ItemType Directory -Path $path -Force
            Write-Host "Created default directory: $path" -ForegroundColor Yellow
        }
    }
}

# Create filename with timestamp
$filename = "ProcessList_$(Get-Date -Format 'yyyyMMdd_HHmmss').txt"
$fullPath = Join-Path $path $filename

Write-Host "Collecting running processes data ..." -ForegroundColor Cyan
try {
    # Add summary information at the top of the file
    "`n`n----------------------------------------" | Out-File -FilePath $fullPath
    "Summary Information:" | Out-File -FilePath $fullPath -Append
    "----------------------------------------" | Out-File -FilePath $fullPath -Append
    "Total Processes: $((Get-Process).Count)" | Out-File -FilePath $fullPath -Append
    "Generated on: $(Get-Date)" | Out-File -FilePath $fullPath -Append
    "Computer Name: $env:COMPUTERNAME" | Out-File -FilePath $fullPath -Append

    # Get all process information and save to file
    Get-Process | Select-Object * | 
    Out-File -FilePath $fullPath -Width 200 -Append
    
    # Output success message and path information
    Write-Host "Process information successfully saved!" -ForegroundColor Green
    if ($path -eq "E:\ASO\PowerShellComands\output") {
        Write-Host "Saved to DEFAULT path: $fullPath" -ForegroundColor Yellow
    } else {
        Write-Host "Saved to SPECIFIED path: $fullPath" -ForegroundColor Cyan
    }
}
catch {
    Write-Host "Error saving process information: $($_.Exception.Message)" -ForegroundColor Red
}