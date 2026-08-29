[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet('local', 'cloud')]
    [string]$Target = 'local',

    [switch]$Check,
    [switch]$ListTables,

    [string]$CloudProperties = 'C:\Cloud Drive\Mega\Dev Projects\Springboot Lab\src\main\resources\application.properties'
)

$ErrorActionPreference = 'Stop'

function Get-JavaProperty {
    param(
        [string[]]$Lines,
        [string]$Name,
        [switch]$AllowCommented
    )

    $prefix = if ($AllowCommented) { '^\s*#?\s*' } else { '^\s*' }
    $match = $Lines | Where-Object { $_ -match "$prefix$([regex]::Escape($Name))\s*=" } | Select-Object -First 1
    if (-not $match) {
        throw "Property '$Name' was not found."
    }

    return ($match -replace "$prefix$([regex]::Escape($Name))\s*=\s*", '').Trim()
}

function Find-Psql {
    $command = Get-Command psql -ErrorAction SilentlyContinue
    if ($command) {
        return $command.Source
    }

    $installed = Get-ChildItem 'C:\Program Files\PostgreSQL\*\bin\psql.exe' -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending |
        Select-Object -First 1
    if ($installed) {
        return $installed.FullName
    }

    throw 'psql was not found. Install PostgreSQL command-line tools or add its bin directory to PATH.'
}

$psql = Find-Psql
$connectionParts = @('connect_timeout=10')

if ($Target -eq 'local') {
    $jdbcUrl = if ($env:DB_LOCAL_URL) { $env:DB_LOCAL_URL } else { 'jdbc:postgresql://localhost:5432/postgres' }
    $username = if ($env:DB_LOCAL_USERNAME) { $env:DB_LOCAL_USERNAME } else { 'postgres' }
    $password = $env:DB_LOCAL_PASSWORD
    if (-not $password) {
        throw 'DB_LOCAL_PASSWORD is not set. Follow Chapter 14.1 in Springboot Gradle Lab.md.'
    }

    if ($jdbcUrl -notmatch '^jdbc:postgresql://([^:/]+):(\d+)/([^?]+)') {
        throw "Unsupported local JDBC URL: $jdbcUrl"
    }

    $connectionParts += "host='$($Matches[1])'", "port='$($Matches[2])'", "dbname='$($Matches[3])'", "user='$username'"
}
else {
    $jdbcUrl = $env:DB_CLOUD_URL
    $username = $env:DB_CLOUD_USERNAME
    $password = $env:DB_CLOUD_PASSWORD

    # Backward-compatible discovery on this PC; environment variables make the
    # launcher portable and are the required setup on a new machine.
    if ((-not $jdbcUrl -or -not $username -or -not $password) -and (Test-Path -LiteralPath $CloudProperties)) {
        $properties = Get-Content -LiteralPath $CloudProperties
        if (-not $jdbcUrl) { $jdbcUrl = Get-JavaProperty $properties 'spring.datasource.url' }
        if (-not $username) { $username = Get-JavaProperty $properties 'spring.datasource.username' }
        if (-not $password) { $password = Get-JavaProperty $properties 'spring.datasource.password' }
    }

    if (-not $jdbcUrl -or -not $username -or -not $password) {
        throw 'Set DB_CLOUD_URL, DB_CLOUD_USERNAME, and DB_CLOUD_PASSWORD. Follow Chapter 14.1 in Springboot Gradle Lab.md.'
    }

    if ($jdbcUrl -notmatch '^jdbc:postgresql://([^:/]+):(\d+)/([^?]+)') {
        throw "Unsupported CockroachDB JDBC URL in: $CloudProperties"
    }

    $hostName = $Matches[1]
    $port = $Matches[2]
    $database = $Matches[3]
    $rootCertificate = if ($env:DB_CLOUD_ROOT_CERT) {
        $env:DB_CLOUD_ROOT_CERT
    } elseif ($jdbcUrl -match '[?&]sslrootcert=([^&]+)') {
        [uri]::UnescapeDataString($Matches[1]).Replace('\ ', ' ')
    } else {
        throw 'CockroachDB URL must specify sslrootcert for verified TLS.'
    }

    if (-not (Test-Path -LiteralPath $rootCertificate)) {
        throw "CockroachDB root certificate not found: $rootCertificate"
    }

    $connectionParts += "host='$hostName'", "port='$port'", "dbname='$database'", "user='$username'",
        'sslmode=verify-full', "sslrootcert='$rootCertificate'"
}

$env:PGPASSWORD = $password
try {
    $connection = $connectionParts -join ' '

    if ($Check) {
        & $psql -X -w --dbname=$connection -v ON_ERROR_STOP=1 -c 'SELECT version(), current_database(), current_user;'
    }
    elseif ($ListTables) {
        & $psql -X -w --dbname=$connection -v ON_ERROR_STOP=1 -c "SELECT table_schema, table_name FROM information_schema.tables WHERE table_schema NOT IN ('pg_catalog', 'information_schema', 'crdb_internal') AND table_type = 'BASE TABLE' ORDER BY 1, 2;"
    }
    else {
        Write-Host "Opening $Target database shell. Use \q to exit and \dt *.* to list tables."
        & $psql -X -w --dbname=$connection
    }

    if ($LASTEXITCODE -ne 0) {
        throw "psql exited with code $LASTEXITCODE."
    }
}
finally {
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}
