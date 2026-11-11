# Render 1 SVG (Design System / Screen Flow) ra PNG dung kich thuoc that
param(
  [string]$Match = 'Design System',
  [string]$Out = 'ds.png',
  [double]$Scale = 1.0
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$prev = Join-Path $root 'preview'
$f = Get-ChildItem $prev -Filter '*.svg' | Where-Object { $_.Name -like "*$Match*" } | Select-Object -First 1
if (-not $f) { throw "Khong tim thay SVG khop $Match" }
$head = [System.IO.File]::ReadAllText($f.FullName, [System.Text.Encoding]::UTF8).Substring(0, 400)
$mw = [regex]::Match($head, 'width="([\d.]+)"')
$mh = [regex]::Match($head, 'height="([\d.]+)"')
$w = [int]([double]$mw.Groups[1].Value * $Scale)
$h = [int]([double]$mh.Groups[1].Value * $Scale)
$b64 = [Convert]::ToBase64String([System.IO.File]::ReadAllBytes($f.FullName))
$html = "<!doctype html><meta charset='utf-8'><style>body{margin:0;background:#fff}img{display:block}</style>" +
  "<img src='data:image/svg+xml;base64,$b64' width='$w' height='$h'>"
$htmlPath = Join-Path $prev 'one.html'
[System.IO.File]::WriteAllText($htmlPath, $html, (New-Object System.Text.UTF8Encoding($false)))

$edge = 'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'
$outPath = Join-Path $prev $Out
if (Test-Path $outPath) { Remove-Item $outPath }
$ErrorActionPreference = 'Continue'
try {
  & $edge --headless --disable-gpu --no-sandbox --hide-scrollbars `
    --user-data-dir="$env:TEMP\edgeshot" --window-size="$w,$h" --screenshot="$outPath" `
    "file:///$($htmlPath -replace '\\','/')" 2>&1 | Out-Null
} catch { }
Start-Sleep -Milliseconds 1200
if (Test-Path $outPath) { "OK $outPath $([int]((Get-Item $outPath).Length/1KB)) KB ($w x $h)" } else { "FAIL" }
