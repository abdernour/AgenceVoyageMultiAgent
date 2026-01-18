# Build and Run Script for AgenceVoyageMultiAgent
# This script compiles and runs the JADE multi-agent travel agency system

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  AgenceVoyageMultiAgent Build & Run" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set paths
$srcDir = "src"
$outDir = "out"
$libDir = "lib"
$mainClass = "com.agencevoyage.MainLauncher"

# Create output directory if it doesn't exist
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir | Out-Null
    Write-Host "Created output directory: $outDir" -ForegroundColor Green
}

# Build classpath from JAR files in lib directory
$classpath = ""
Get-ChildItem -Path $libDir -Filter "*.jar" | ForEach-Object {
    if ($classpath) {
        $classpath += ";"
    }
    $classpath += $_.FullName
}

if (-not $classpath) {
    Write-Host "ERROR: No JAR files found in $libDir directory!" -ForegroundColor Red
    Write-Host "Please ensure the following JAR files are in the lib folder:" -ForegroundColor Yellow
    Write-Host "  - jade.jar" -ForegroundColor Yellow
    Write-Host "  - mysql-connector-j-9.5.0.jar" -ForegroundColor Yellow
    Write-Host "  - jcalendar-1.4.jar" -ForegroundColor Yellow
    exit 1
}

Write-Host "Classpath: $classpath" -ForegroundColor Gray
Write-Host ""

# Compile Java files
Write-Host "Compiling Java sources..." -ForegroundColor Yellow

# Find all Java files recursively
$javaFiles = Get-ChildItem -Path $srcDir -Filter "*.java" -Recurse
if ($javaFiles.Count -eq 0) {
    Write-Host "ERROR: No Java files found in $srcDir!" -ForegroundColor Red
    exit 1
}

# Compile using javac with all files
$javacArgs = @("-encoding", "UTF-8", "-d", $outDir, "-cp", $classpath)
$javacArgs += $javaFiles.FullName

try {
    & javac $javacArgs
    if ($LASTEXITCODE -eq 0 -or $LASTEXITCODE -eq $null) {
        Write-Host "[OK] Compilation successful!" -ForegroundColor Green
    } else {
        Write-Host "[ERROR] Compilation failed (exit code: $LASTEXITCODE)!" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "[ERROR] Compilation error: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""

# Run the application
Write-Host "Starting application..." -ForegroundColor Yellow
Write-Host ""

$runClasspath = "$outDir;$classpath"

try {
    & java -cp $runClasspath $mainClass
} catch {
    Write-Host "[ERROR] Error running application: $_" -ForegroundColor Red
    exit 1
}
