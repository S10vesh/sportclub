$candidates = @(
    "$env:MAVEN_HOME\bin\mvn.cmd",
    "$env:TEMP\apache-maven-3.9.11\bin\mvn.cmd",
    "C:\Program Files\Apache Maven\bin\mvn.cmd"
)

$maven = $candidates | Where-Object { $_ -and (Test-Path $_) } | Select-Object -First 1

if (-not $maven) {
    $mavenCommand = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($mavenCommand) {
        $maven = $mavenCommand.Source
    }
}

if (-not $maven) {
    Write-Error "Apache Maven не найден. Установите Maven и задайте переменную MAVEN_HOME."
    exit 1
}

Write-Host "Используется Apache Maven: $maven"
& $maven clean compile exec:java
exit $LASTEXITCODE
