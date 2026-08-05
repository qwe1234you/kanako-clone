# Kanako Clone 本地开发/生产启动脚本（Windows）
# 用法:
#   .\scripts\dev.ps1          # 启动后端 + 前端 dev（开发）
#   .\scripts\build.ps1        # 构建前端产物 + 打包后端 jar

param(
    [switch]$Prod
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
$backend = Join-Path $root "backend"
$frontend = Join-Path $root "frontend"

function Ensure-Tools {
    $env:Path = [Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [Environment]::GetEnvironmentVariable("Path", "User")
    if (-not $env:JAVA_HOME) {
        $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.8-hotspot"
    }
    if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
        throw "node 未安装，请先安装 Node.js"
    }
    if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
        throw "maven 未安装或不在 PATH"
    }
}

function Start-Backend {
    Write-Host "==> 打包后端..."
    Push-Location $backend
    mvn -q package -DskipTests
    Pop-Location
    $jar = Join-Path $backend "target\kanako-backend-1.0.0.jar"
    if (-not (Test-Path $jar)) { throw "jar 构建失败: $jar" }
    Write-Host "==> 启动后端 (java -jar)..."
    $args = @("-jar", $jar)
    if ($Prod) { $args += "--spring.profiles.active=prod" }
    Start-Process -FilePath (Join-Path $env:JAVA_HOME "bin\java.exe") -ArgumentList $args `
        -WorkingDirectory $backend -WindowStyle Hidden
    Write-Host "    后端已启动，日志: $backend\run.log"
}

if ($Prod) {
    Ensure-Tools
    Start-Backend
    Write-Host "生产模式：请用 nginx 托管 frontend/dist（参考 deploy/nginx.conf）"
    exit 0
}

Ensure-Tools
Start-Backend

Write-Host "==> 启动前端 dev..."
Push-Location $frontend
$nodeExe = Get-Command node | Select-Object -ExpandProperty Source
Start-Process -FilePath $nodeExe -ArgumentList @("node_modules\vite\bin\vite.js") `
    -WorkingDirectory $frontend -WindowStyle Hidden
Pop-Location

Write-Host ""
Write-Host "前端: http://localhost:5173"
Write-Host "后端: http://localhost:8080  (admin / admin123)"
