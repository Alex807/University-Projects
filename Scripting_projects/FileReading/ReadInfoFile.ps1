# definim toate cazurile ce pot fi acoperite pentru a face scriptul cat mai extensibil
param(
    [Parameter(Mandatory=$true, #caz de baza in care trebuie specificat doar un path catre fisier
               Position=0,
               HelpMessage="Enter the path to info.txt file")]
    [string]$FilePath,
    
    [Parameter(Mandatory=$false, 
               HelpMessage="Number of lines to read")]
    [int]$NumberOfLines = 3, #pentru acest exemplu am harcodat cu 3 linii, dar poate fi modificat folosind valoarea de la linia de comanda
    
    [Parameter(Mandatory=$false,
               HelpMessage="Specify allowed file extensions")]
    [string[]]$AllowedExtensions = @('.txt'), #putem cauta si alte extensii pe viitor, cum ar fi .csv de exemplu
    
    [Parameter(Mandatory=$false,
               HelpMessage="Specify allowed file names")]
    [string[]]$AllowedFileNames = @('info.txt') #putem restrictiona drept input ce fisiere au voie sa fie procesate de acest script
)

function Print-Help { 
    # cazul nostru de baza
    .\Script.ps1 "PATH\TO\info.txt"

    # atunci cand dorim sa citim un anumit numar de linii
    .\Script.ps1 ".\info.txt" -NumberOfLines 5

    # putem ajusta ce extensii sunt permise pentru fisierele pe care le vom citi, respectiv ce fisiere anume dorim sa fie procesate
    .\Script.ps1 ".\data.txt" -AllowedExtensions @('.txt', '.log') -AllowedFileNames @('info.txt', 'data.txt')
}

#functie de validare a inputurilor primite
function Test-FilePath {
    param (
        [string]$Path,
        [string[]]$ValidExtensions,
        [string[]]$ValidFileNames
    )
    
    # caz in care calea este nula sau goala
    if ([string]::IsNullOrWhiteSpace($Path)) {
        throw "File path cannot be empty"
    }

    # verificam daca calea exista
    if (-not (Test-Path -Path $Path)) {
        throw "File path does not exist: $Path" #am decis ca in cazul in care calea nu exista, sa nu creez un fisier nou, deoarece acesta ar fi gol si nu am avea ce procesa
                                                # prin urmare am decis sa arunc o exceptie
    }

    # obtinem informatiile despre fisier
    $file = Get-Item $Path

    # verificam daca este un fisier
    if ($file -isnot [System.IO.FileInfo]) {
        throw "The specified path is not a file: $Path"
    }

    # ne asiguram ca este suportata aceasta extensie
    if (-not ($ValidExtensions -contains $file.Extension)) {
        throw "Invalid file extension. Allowed extensions are: $($ValidExtensions -join ', ')"
    }

    # ne asiguram ca fisierul poate fi procesat de script
    if (-not ($ValidFileNames -contains $file.Name)) {
        throw "Invalid file name. Allowed names are: $($ValidFileNames -join ', ')"
    }

    # verificam drepturile de citire la fisier, nu scriem in el nimic
    try {
        $null = Get-Content -Path $Path -TotalCount 1 -ErrorAction Stop
    }
    catch {
        throw "Cannot read the file. Access denied or file is locked."
    }

    return $true
}

# Main script 
try {
    # caz in care tratam calea relativa la fisier
    $resolvedPath = Resolve-Path $FilePath -ErrorAction Stop
    
    # apelam functia de validare a inputurilor
    Write-Host "Validating file path..." -ForegroundColor Yellow
    Test-FilePath -Path $resolvedPath -ValidExtensions $AllowedExtensions -ValidFileNames $AllowedFileNames
    
    Write-Host "File validation successful!" -ForegroundColor Green
    Write-Host "Reading first $NumberOfLines lines from: $resolvedPath`n" -ForegroundColor Cyan

    #citim continutul fisierului
    $content = Get-Content -Path $resolvedPath -TotalCount $NumberOfLines -ErrorAction Stop
    
    if ($content.Count -eq 0) { #caz in care fisierul este gol
        Write-Warning "The file is empty!"
    }
    else {
        # printam continutul fisierului
        for($i = 0; $i -lt $content.Count; $i++) {
            Write-Host "Line $($i+1): $($content[$i])"
        }

        # afisam un mesaj de avertizare in cazul in care numarul de linii citite este mai mic decat cel specificat, dar afisam totusi continutul sau
        if ($content.Count -lt $NumberOfLines) {
            Write-Warning "File contains only $($content.Count) line(s)"
        }
    }
}
catch { #caz in care nu am reusit sa citim fisierul deoarece nu am avut drepturi de acces sau fisierul este blocat
    Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}