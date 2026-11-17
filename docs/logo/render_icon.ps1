# Vẽ icon NutriVision ra PNG: dự phòng cho launcher Android 7/7.1 (chưa hỗ trợ adaptive icon)
# và xuất bản 512 px cho tài liệu.
#
# Cùng hình học với res/drawable/ic_launcher_foreground.xml (viewport 108 × 108).
# Chạy: powershell -ExecutionPolicy Bypass -File docs/logo/render_icon.ps1
param(
    [string]$RepoRoot = (Split-Path -Parent (Split-Path -Parent $PSScriptRoot))
)

Add-Type -AssemblyName System.Drawing

function New-Color([int]$a, [int]$r, [int]$g, [int]$b) {
    return [System.Drawing.Color]::FromArgb($a, $r, $g, $b)
}

function Add-ScaledBezier($path, [double[]]$p, [double]$k) {
    # p: x0,y0, c1x,c1y, c2x,c2y, x1,y1 (đơn vị 108)
    $path.AddBezier(
        [float]($p[0] * $k), [float]($p[1] * $k),
        [float]($p[2] * $k), [float]($p[3] * $k),
        [float]($p[4] * $k), [float]($p[5] * $k),
        [float]($p[6] * $k), [float]($p[7] * $k)
    )
}

function Draw-Icon([int]$size, [string]$outPath, [bool]$circle) {
    $k = $size / 108.0
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $g.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

    $rect = New-Object System.Drawing.RectangleF 0, 0, $size, $size

    # --- nền gradient ---
    $bg = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
        $rect,
        (New-Color 255 10 117 81),
        (New-Color 255 23 190 132),
        [float]45)
    $blend = New-Object System.Drawing.Drawing2D.ColorBlend 3
    $blend.Colors = @((New-Color 255 10 117 81), (New-Color 255 14 157 107), (New-Color 255 23 190 132))
    $blend.Positions = @([float]0.0, [float]0.55, [float]1.0)
    $bg.InterpolationColors = $blend
    $g.FillRectangle($bg, $rect)

    # --- quầng sáng góc trên trái ---
    $cx = 26 * $k; $cy = 17 * $k; $r = 88 * $k
    $glowPath = New-Object System.Drawing.Drawing2D.GraphicsPath
    $glowPath.AddEllipse([float]($cx - $r), [float]($cy - $r), [float](2 * $r), [float](2 * $r))
    $glow = New-Object System.Drawing.Drawing2D.PathGradientBrush($glowPath)
    $glow.CenterColor = New-Color 61 255 255 255
    $glow.SurroundColors = @((New-Color 0 255 255 255))
    $g.FillPath($glow, $glowPath)

    # --- zoom out tỉ lệ 0.72 quanh tâm (54, 54) để thoáng đãng ---
    $cxCenter = 54.0 * $k
    $cyCenter = 54.0 * $k
    $scale = 0.72
    $g.TranslateTransform($cxCenter, $cyCenter)
    $g.ScaleTransform($scale, $scale)
    $g.TranslateTransform(-$cxCenter, -$cyCenter)

    # --- quầng lá ---
    $halo = New-Object System.Drawing.SolidBrush (New-Color 36 163 230 53)
    $g.FillEllipse($halo, [float]((61 - 27) * $k), [float]((61 - 27) * $k), [float](54 * $k), [float](54 * $k))

    # --- lá ---
    $leaf = New-Object System.Drawing.Drawing2D.GraphicsPath
    $leaf.StartFigure()
    Add-ScaledBezier $leaf @(36, 76, 33, 52, 47, 35, 73, 31) $k
    Add-ScaledBezier $leaf @(73, 31, 75, 57, 63, 74, 36, 76) $k
    $leaf.CloseFigure()
    $leafBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
    $g.FillPath($leafBrush, $leaf)

    # --- gân lá ---
    $veinPen = New-Object System.Drawing.Pen (New-Color 230 14 157 107), ([float](3 * $k))
    $veinPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $veinPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawBezier($veinPen,
        [float](37 * $k), [float](75 * $k), [float](44 * $k), [float](66 * $k),
        [float](54 * $k), [float](54 * $k), [float](66 * $k), [float](41 * $k))

    # --- cuống ---
    $stemPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::White), ([float](3.4 * $k))
    $stemPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $stemPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $g.DrawBezier($stemPen,
        [float](36 * $k), [float](76 * $k), [float](34 * $k), [float](80 * $k),
        [float](32 * $k), [float](83 * $k), [float](29 * $k), [float](86 * $k))

    # --- tia sáng ---
    $spark = New-Object System.Drawing.Drawing2D.GraphicsPath
    $spark.StartFigure()
    Add-ScaledBezier $spark @(79, 23.4, 79.6, 26.2, 81.4, 28, 84.2, 28.6) $k
    Add-ScaledBezier $spark @(84.2, 28.6, 81.4, 29.2, 79.6, 31, 79, 33.8) $k
    Add-ScaledBezier $spark @(79, 33.8, 78.4, 31, 76.6, 29.2, 73.8, 28.6) $k
    Add-ScaledBezier $spark @(73.8, 28.6, 76.6, 28, 78.4, 26.2, 79, 23.4) $k
    $spark.CloseFigure()
    $sparkBrush = New-Object System.Drawing.SolidBrush (New-Color 217 255 255 255)
    $g.FillPath($sparkBrush, $spark)

    $g.ResetTransform()

    $g.Dispose()

    if ($circle) {
        # Cắt tròn cho ic_launcher_round (Android 7.1)
        $masked = New-Object System.Drawing.Bitmap $size, $size
        $mg = [System.Drawing.Graphics]::FromImage($masked)
        $mg.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $mg.Clear([System.Drawing.Color]::Transparent)
        $clip = New-Object System.Drawing.Drawing2D.GraphicsPath
        $clip.AddEllipse(0, 0, $size, $size)
        $mg.SetClip($clip)
        $mg.DrawImage($bmp, 0, 0, $size, $size)
        $mg.Dispose()
        $bmp.Dispose()
        $bmp = $masked
    }

    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Output ("  " + $outPath)
}

$res = Join-Path $RepoRoot "app/src/main/res"
$densities = [ordered]@{ "mdpi" = 48; "hdpi" = 72; "xhdpi" = 96; "xxhdpi" = 144; "xxxhdpi" = 192 }

Write-Output "Legacy launcher icons:"
foreach ($name in $densities.Keys) {
    $dir = Join-Path $res ("mipmap-" + $name)
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    $size = $densities[$name]
    Draw-Icon $size (Join-Path $dir "ic_launcher.png") $false
    Draw-Icon $size (Join-Path $dir "ic_launcher_round.png") $true
}

$logoDir = Join-Path $RepoRoot "docs/logo"
New-Item -ItemType Directory -Force -Path $logoDir | Out-Null
Write-Output "Bản tài liệu:"
Draw-Icon 512 (Join-Path $logoDir "nutrivision_icon_512.png") $false
