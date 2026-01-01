#!/usr/bin/env pwsh
<#
PowerShell equivalent of devMode.sh

Resolves the script directory, assembles the classpath (preferring jars next to this
script, otherwise falling back to lib/gwt-2.12.2), adds optional GWT extras and
project source/target directories, and runs the GWT CodeServer.
#>

# Resolve this script's directory to an absolute path (works when invoked from elsewhere)
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$ScriptDir = (Resolve-Path $ScriptDir).Path

# Prefer jars next to this script (original behavior), otherwise fall back to repo layout
if (Test-Path (Join-Path $ScriptDir 'gwt-codeserver.jar')) {
    $GWT_DIR = $ScriptDir
} else {
    $GWT_DIR = Join-Path $ScriptDir 'lib/gwt-2.12.2'
}

# Optional GWT-provided extras
$VALIDATION_API_JAR = Join-Path $GWT_DIR 'validation-api-1.0.0.GA.jar'
$VALIDATION_API_SOURCES_JAR = Join-Path $GWT_DIR 'validation-api-1.0.0.GA-sources.jar'

$cpParts = @()
$cpParts += (Join-Path $GWT_DIR 'gwt-codeserver.jar')
$cpParts += (Join-Path $GWT_DIR 'gwt-user.jar')
$cpParts += (Join-Path $GWT_DIR 'gwt-dev.jar')

if (Test-Path $VALIDATION_API_JAR) { $cpParts += $VALIDATION_API_JAR }
if (Test-Path $VALIDATION_API_SOURCES_JAR) { $cpParts += $VALIDATION_API_SOURCES_JAR }

# Add project sources to the classpath so CodeServer can see .java and .gwt.xml sources.
$CLIENT_SRC = Join-Path $ScriptDir 'src/main/java'
$SHARED_SRC = Join-Path $ScriptDir '..' | Join-Path -ChildPath 'modular-webapp-shared/src/main/java'
$CLIENT_CLASSES = Join-Path $ScriptDir 'target/classes'
$SHARED_CLASSES = Join-Path $ScriptDir '..' | Join-Path -ChildPath 'modular-webapp-shared/target/classes'

foreach ($p in @($CLIENT_SRC, $SHARED_SRC, $CLIENT_CLASSES, $SHARED_CLASSES)) {
    if (Test-Path $p) { $cpParts += $p }
}

# Use platform-specific path separator for classpath (':' on Unix/macOS, ';' on Windows)
$sep = [System.IO.Path]::PathSeparator
$classpath = $cpParts -join $sep

# Optional debug output: run with DEBUG_CP=1 to print the extra classpath entries.
if ($env:DEBUG_CP -and $env:DEBUG_CP -ne '0') { Write-Output $classpath }

# Build java arguments and pass through any script arguments
$javaArgs = @('-cp', $classpath, 'com.google.gwt.dev.DevMode', '-noincremental', '-startupUrl', 'http://localhost:8080')
if ($args) { $javaArgs += $args }

& java @javaArgs
exit $LASTEXITCODE
