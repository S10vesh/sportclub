$postgresBin = 'C:\Program Files\PostgreSQL\18\bin'
$createdb = Join-Path $postgresBin 'createdb.exe'
$psql = Join-Path $postgresBin 'psql.exe'
$schema = Join-Path $PSScriptRoot 'schema.sql'

if (-not (Test-Path $createdb) -or -not (Test-Path $psql)) {
    throw "PostgreSQL tools were not found in $postgresBin"
}

Write-Host 'Creating database sportclub (already existing database is allowed)...'
& $createdb -h localhost -U postgres sportclub 2>$null

if ($LASTEXITCODE -ne 0) {
    Write-Host 'Database already exists or PostgreSQL requested a password.'
}

Write-Host 'Applying database/schema.sql...'
$previousClientEncoding = $env:PGCLIENTENCODING
$env:PGCLIENTENCODING = 'UTF8'
& $psql -h localhost -U postgres -d sportclub -v ON_ERROR_STOP=1 -f $schema
$env:PGCLIENTENCODING = $previousClientEncoding

if ($LASTEXITCODE -ne 0) {
    throw 'Could not apply schema.sql. Check the SQL output above.'
}

Write-Host 'Done. Database sportclub and tables are initialized.'
