#!/usr/bin/env pwsh
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
  [int]$DebounceMs = 750,
  [switch]$DebugEvents,
  [switch]$Polling = $true
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
Write-Host "Debug events: $DebugEvents"
Write-Host "Polling mode: $Polling"
Write-Host ""

# Debounce timer: any file event resets it; when it elapses, we hit /recompile once.
$timer = New-Object System.Timers.Timer
$timer.Interval = [double]$DebounceMs
$timer.AutoReset = $false

$triggerRequested = $false

# Store all event subscribers for cleanup
$eventSubscribers = @()

function Invoke-Recompile() {
  $ts = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
  Write-Host -NoNewline "[$ts] change detected -> recompile... "
  try {
    Invoke-WebRequest -Uri $Url -Method GET -UseBasicParsing | Out-Null
    Write-Host "ok"
  } catch {
    Write-Host "failed (CodeServer running on :9876?)"
  }
}

$onTimer = Register-ObjectEvent -InputObject $timer -EventName Elapsed -Action {
  if (-not $script:triggerRequested) { return }
  $script:triggerRequested = $false

  Invoke-Recompile
}
$eventSubscribers += $onTimer

function New-Watcher([string]$path) {
  $w = New-Object System.IO.FileSystemWatcher
  $w.Path = $path
  $w.Filter = "*.*"
  $w.IncludeSubdirectories = $true
  $w.NotifyFilter = [System.IO.NotifyFilters]'FileName, LastWrite, DirectoryName, Size'
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
    $subscriber = Register-ObjectEvent -InputObject $w -EventName $evt -Action {
      $fullPath = $null
      if ($EventArgs -and $EventArgs.PSObject.Properties.Match("FullPath").Count -gt 0) {
        $fullPath = [string]$EventArgs.FullPath
      }
      if ($fullPath -and (Should-Ignore $fullPath)) {
        if ($using:DebugEvents) {
          Write-Host "Ignored: $fullPath"
        }
        return
      }

      if ($using:DebugEvents) {
        $eventName = $EventArgs.GetType().Name
        Write-Host "Event: $eventName $fullPath"
      }
      $script:triggerRequested = $true
      $timer.Stop()
      $timer.Start()
    }
    $eventSubscribers += $subscriber
  }
}

function Get-TrackedFiles([string]$path) {
  Get-ChildItem -Path $path -Recurse -File -ErrorAction SilentlyContinue | Where-Object {
    -not (Should-Ignore $_.FullName)
  }
}

function Start-PollingLoop() {
  $fileState = @{}
  foreach ($p in @($clientSrc, $sharedSrc)) {
    foreach ($f in (Get-TrackedFiles $p)) {
      $fileState[$f.FullName] = $f.LastWriteTimeUtc
    }
  }

  $pending = $false
  $lastChange = [DateTime]::MinValue

  while ($true) {
    $changed = $false
    foreach ($p in @($clientSrc, $sharedSrc)) {
      foreach ($f in (Get-TrackedFiles $p)) {
        $last = $fileState[$f.FullName]
        if (-not $last) {
          $fileState[$f.FullName] = $f.LastWriteTimeUtc
          $changed = $true
          if ($DebugEvents) { Write-Host "Event: NewFile $($f.FullName)" }
        } elseif ($f.LastWriteTimeUtc -ne $last) {
          $fileState[$f.FullName] = $f.LastWriteTimeUtc
          $changed = $true
          if ($DebugEvents) { Write-Host "Event: Modified $($f.FullName)" }
        }
      }
    }

    if ($changed) {
      $pending = $true
      $lastChange = [DateTime]::UtcNow
    }

    if ($pending) {
      $elapsedMs = ([DateTime]::UtcNow - $lastChange).TotalMilliseconds
      if ($elapsedMs -ge $DebounceMs) {
        $pending = $false
        Invoke-Recompile
      }
    }

    Start-Sleep -Milliseconds 1000
  }
}

try {
  if ($Polling) {
    Start-PollingLoop
  } else {
    while ($true) { Start-Sleep -Seconds 1 }
  }
} finally {
  $timer.Stop()
  # Unregister all event subscribers
  foreach ($subscriber in $eventSubscribers) {
    if ($subscriber -and $subscriber.Name) {
      Unregister-Event -SourceIdentifier $subscriber.Name -ErrorAction SilentlyContinue
    }
  }
  $timer.Dispose()
  foreach ($w in $watchers) {
    $w.EnableRaisingEvents = $false
    $w.Dispose()
  }
}

