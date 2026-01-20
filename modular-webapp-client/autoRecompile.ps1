<#
Auto-recompile watcher for GWT Super Dev Mode.

Watches:
- modular-webapp-client/src/main/java
- modular-webapp-shared/src/main/java

Triggers:
  GET http://localhost:9876/recompile/modularwebapp

Usage:
  pwsh ./autoRecompile.ps1
  pwsh ./autoRecompile.ps1 -Url "http://localhost:9876/recompile/modularwebapp"

Notes:
- This script is PowerShell-only and does not require extra dependencies.
- For bash on macOS, use autoRecompile.sh (requires fswatch).
#>

[CmdletBinding()]
param(
  [string]$Url = "http://localhost:9876/recompile/modularwebapp",
  [int]$DebounceMs = 750
)

$ErrorActionPreference = "Stop"

function Get-RepoRoot([string]$scriptDir) {
  return (Resolve-Path (Join-Path $scriptDir "..")).Path
}

$scriptDir = (Resolve-Path (Split-Path -Parent $MyInvocation.MyCommand.Definition)).Path
$repoRoot = Get-RepoRoot $scriptDir

$clientSrc = Join-Path $repoRoot "modular-webapp-client/src/main/java"
$sharedSrc = Join-Path $repoRoot "modular-webapp-shared/src/main/java"

foreach ($p in @($clientSrc, $sharedSrc)) {
  if (-not (Test-Path $p)) {
    Write-Error "Path not found: $p"
    exit 2
  }
}

Write-Host "Watching for changes:"
Write-Host " - $clientSrc"
Write-Host " - $sharedSrc"
Write-Host "Debounce: ${DebounceMs}ms"
Write-Host "Recompile URL: $Url"
Write-Host ""

# Debounce timer: any file event resets it; when it elapses, we hit /recompile once.
$timer = New-Object System.Timers.Timer
$timer.Interval = [double]$DebounceMs
$timer.AutoReset = $false

$triggerRequested = $false

$onTimer = Register-ObjectEvent -InputObject $timer -EventName Elapsed -Action {
  if (-not $script:triggerRequested) { return }
  $script:triggerRequested = $false

  $ts = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
  Write-Host -NoNewline "[$ts] change detected -> recompile... "
  try {
    Invoke-WebRequest -Uri $using:Url -Method GET -UseBasicParsing | Out-Null
    Write-Host "ok"
  } catch {
    Write-Host "failed (CodeServer running on :9876?)"
  }
}

function New-Watcher([string]$path) {
  $w = New-Object System.IO.FileSystemWatcher
  $w.Path = $path
  $w.IncludeSubdirectories = $true
  $w.NotifyFilter = [System.IO.NotifyFilters]'FileName, LastWrite, DirectoryName'
  $w.EnableRaisingEvents = $true
  return $w
}

$watchers = @(
  (New-Watcher $clientSrc),
  (New-Watcher $sharedSrc)
)

function Should-Ignore([string]$fullPath) {
  # Normalize path separators for a simple contains-check
  $p = $fullPath -replace "\\", "/"
  return ($p -match "/(\.git|target|war|lib)/")
}

foreach ($w in $watchers) {
  foreach ($evt in @("Changed", "Created", "Deleted", "Renamed")) {
    Register-ObjectEvent -InputObject $w -EventName $evt -Action {
      $fullPath = $null
      if ($EventArgs -and $EventArgs.PSObject.Properties.Match("FullPath").Count -gt 0) {
        $fullPath = [string]$EventArgs.FullPath
      }
      if ($fullPath -and (Should-Ignore $fullPath)) { return }

      $script:triggerRequested = $true
      $timer.Stop()
      $timer.Start()
    } | Out-Null
  }
}

try {
  while ($true) { Start-Sleep -Seconds 1 }
} finally {
  $timer.Stop()
  Unregister-Event -SourceIdentifier $onTimer.Name -ErrorAction SilentlyContinue
  $timer.Dispose()
  foreach ($w in $watchers) { $w.Dispose() }
}

