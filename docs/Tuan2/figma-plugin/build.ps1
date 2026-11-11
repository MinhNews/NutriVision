# Build src/*.js into code.js (Figma runs a single main file)
$ErrorActionPreference = 'Stop'
$root = $PSScriptRoot
$out  = Join-Path $root 'code.js'
$files = @(
  '01_core.js',
  '02_atoms.js',
  '10_screens_a.js',
  '11_screens_b.js',
  '20_designsystem.js',
  '21_flow.js',
  '99_main.js'
)
$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('// AUTO-GENERATED from src/*.js - run build.ps1 to rebuild')
foreach ($f in $files) {
  $p = Join-Path $root "src\$f"
  if (-not (Test-Path $p)) { throw "Missing file $p" }
  [void]$sb.AppendLine("")
  [void]$sb.AppendLine("// ===== $f =====")
  [void]$sb.AppendLine((Get-Content $p -Raw -Encoding UTF8))
}
[System.IO.File]::WriteAllText($out, $sb.ToString(), (New-Object System.Text.UTF8Encoding($false)))
Write-Host ("Built code.js ({0:N0} KB)" -f ((Get-Item $out).Length / 1KB))
