#!/usr/bin/env pwsh
<#
PowerShell equivalent of devModeWithParams.sh

Calls devMode.ps1 with a default set of parameters. Any additional parameters
passed to this script are forwarded to devMode.ps1.
#>

$here = Split-Path -Parent $MyInvocation.MyCommand.Definition
$here = (Resolve-Path $here).Path

$devModeScript = Join-Path $here 'devMode.ps1'
if (-not (Test-Path $devModeScript)) {
    Write-Error "Cannot find $devModeScript"
    exit 2
}

# Default parameters (same example as the bash script)
$defaultParams = @('-style', 'PRETTY', 'murdockinfotech.client.ModularWebapp')

# Merge defaults with any user-supplied args (user args appended)
$allParams = $defaultParams + $args

& pwsh -File $devModeScript -- $allParams
exit $LASTEXITCODE
