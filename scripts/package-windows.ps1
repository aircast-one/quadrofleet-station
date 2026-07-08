<#
.SYNOPSIS
  Build a self-contained Windows .msi for QuadroFleet Station.

.DESCRIPTION
  Bundles a JRE plus the GStreamer MSVC runtime DLLs and SDL2 into an .msi via
  jpackage (WiX is required on PATH, preinstalled on GitHub windows-latest).

  NOTE: follows the proven macOS packaging pattern but has NOT yet been
  validated end-to-end. Validate on a CI runner before relying on it.

.PARAMETER Version
  Numeric MAJOR.MINOR.PATCH version, e.g. 1.0.0
#>
param(
  [Parameter(Mandatory = $true)][string]$Version
)
$ErrorActionPreference = "Stop"

$Root = Split-Path -Parent $PSScriptRoot
$AppName = "QuadroFleet Station"
$MainJar = "app.jar"
$MainClass = "com.quadrofleet.Launcher"

$GstRoot = $env:GSTREAMER_1_0_ROOT_MSVC_X86_64
if (-not $GstRoot -or -not (Test-Path (Join-Path $GstRoot "bin"))) {
  # Locate the install root by finding the core runtime DLL (bin's parent).
  $dll = Get-ChildItem "C:\gstreamer" -Recurse -Filter "gstreamer-1.0-0.dll" -ErrorAction SilentlyContinue |
         Select-Object -First 1
  if (-not $dll) {
    $dll = Get-ChildItem "C:\gstreamer" -Recurse -Filter "libgstreamer-1.0-0.dll" -ErrorAction SilentlyContinue |
           Select-Object -First 1
  }
  if (-not $dll) { throw "GStreamer runtime not found under C:\gstreamer" }
  $GstRoot = Split-Path (Split-Path $dll.FullName)
}
Write-Host "> GStreamer root: $GstRoot"

$Build = Join-Path $Root "build"
$Stage = Join-Path $Build "pkg-win"
$Input = Join-Path $Stage "input"
$Dest = Join-Path $Build "dist-win"

$Plugins = @(
  "gstcoreelements.dll","gstudp.dll","gstrtp.dll","gstrtpmanager.dll",
  "gstvideoparsersbad.dll","gstlibav.dll","gstvideoconvertscale.dll",
  "gstvideoscale.dll","gstvideoconvert.dll",
  "gstapp.dll","gsttypefindfunctions.dll","gstplayback.dll","gstautodetect.dll"
)

Write-Host "> Building runtime jars"
Push-Location $Root
& .\gradlew.bat --console=plain -q clean installDist
Pop-Location

Write-Host "> Staging"
Remove-Item -Recurse -Force $Stage, $Dest -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path "$Input\native\gstreamer\gstreamer-1.0", "$Input\native\sdl2", $Dest | Out-Null
Copy-Item "$Root\app\build\install\app\lib\app.jar" $Input

Write-Host "> Bundling GStreamer runtime DLLs"
Copy-Item "$GstRoot\bin\*.dll" "$Input\native\gstreamer\"
foreach ($p in $Plugins) {
  $src = Join-Path "$GstRoot\lib\gstreamer-1.0" $p
  if (Test-Path $src) { Copy-Item $src "$Input\native\gstreamer\gstreamer-1.0\" }
  else { Write-Host "  ! skip $p" }
}

Write-Host "> jpackage"
& jpackage `
  --type msi `
  --name $AppName `
  --app-version $Version `
  --input $Input `
  --main-jar $MainJar `
  --main-class $MainClass `
  --java-options "-Dgstreamer.path=`$APPDIR\native\gstreamer" `
  --java-options "-Dsdl2.path=`$APPDIR\native\sdl2" `
  --java-options "-Djna.library.path=`$APPDIR\native\gstreamer;`$APPDIR\native\sdl2" `
  --win-shortcut --win-menu `
  --dest $Dest

Write-Host "> Done: $(Get-ChildItem $Dest\*.msi | Select-Object -First 1)"
