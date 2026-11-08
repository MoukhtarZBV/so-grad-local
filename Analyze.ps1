[CmdletBinding()]
param(
    [string]$ProjectKey = 'orders-gradle-lab',
    [string]$HostUrl = 'http://localhost:9000'
)
$ErrorActionPreference = 'Stop'
if (-not $env:SONAR_TOKEN) { throw 'Set SONAR_TOKEN in this PowerShell session first.' }
Push-Location $PSScriptRoot
try {
    & .\gradlew.bat clean test jacocoTestReport sonar "-Dsonar.projectKey=$ProjectKey" "-Dsonar.host.url=$HostUrl" '-Dsonar.qualitygate.wait=true' '--console=plain'
    if ($LASTEXITCODE -ne 0) { throw "Build or analysis exited with code $LASTEXITCODE. Read the preceding logs." }
} finally { Pop-Location }
