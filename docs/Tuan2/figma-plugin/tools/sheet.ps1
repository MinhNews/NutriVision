# Contact sheet tu preview/*.svg -> PNG (Edge headless). Nhung SVG dang data-URI de khong phu thuoc file://
param(
  [string]$Prefix = 'M',
  [string]$Pattern = '',
  [string]$Out = 'contact-mock.png',
  [double]$Scale = 0.46,
  [int]$Cols = 5
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$prev = Join-Path $root 'preview'
$files = Get-ChildItem $prev -Filter "$Prefix*.svg" | Sort-Object Name
if ($Pattern -ne '') { $files = $files | Where-Object { $_.BaseName -like $Pattern } }
$items = @()
$i = 0
foreach ($f in $files) {
  $i++
  $b64 = [Convert]::ToBase64String([System.IO.File]::ReadAllBytes($f.FullName))
  $w = [int](390 * $Scale); $h = [int](844 * $Scale)
  $items += "<figure><img src='data:image/svg+xml;base64,$b64' width='$w' height='$h'/><figcaption>$($f.BaseName)</figcaption></figure>"
}
$html = @"
<!doctype html><meta charset='utf-8'>
<style>
 body{margin:0;background:#eef2f7;font-family:Segoe UI,Arial;padding:18px}
 .grid{display:grid;grid-template-columns:repeat($Cols,auto);gap:18px}
 figure{margin:0;background:#fff;padding:6px;border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,.12)}
 img{display:block;border-radius:6px}
 figcaption{font-size:11px;color:#475569;padding:4px 2px 0}
</style>
<div class='grid'>$($items -join "`n")</div>
"@
$htmlPath = Join-Path $prev 'contact.html'
[System.IO.File]::WriteAllText($htmlPath, $html, (New-Object System.Text.UTF8Encoding($false)))

$edge = 'C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe'
$outPath = Join-Path $prev $Out
if (Test-Path $outPath) { Remove-Item $outPath }
$w = [int](($Cols * (390 * $Scale + 18)) + 60)
$h = [int](([math]::Ceiling($files.Count / $Cols) * (844 * $Scale + 46)) + 60)
$profile = Join-Path $env:TEMP 'edgeshot'
$ErrorActionPreference = 'Continue'
try {
  & $edge --headless --disable-gpu --no-sandbox --hide-scrollbars `
    --user-data-dir="$profile" --window-size="$w,$h" --screenshot="$outPath" `
    "file:///$($htmlPath -replace '\\','/')" 2>&1 | Out-Null
} catch { }
Start-Sleep -Milliseconds 1200
if (Test-Path $outPath) { "OK $outPath $([int]((Get-Item $outPath).Length/1KB)) KB ($w x $h)" } else { "FAIL" }
