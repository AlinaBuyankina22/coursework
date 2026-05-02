$ErrorActionPreference = "Stop"

function Test-JavaAvailable {
	return $null -ne (Get-Command java -ErrorAction SilentlyContinue)
}

function Get-JavaMajorVersion {
	$out = (& java -version 2>&1 | Out-String)
	if ($out -match 'version "1\.(\d+)') { return [int]$Matches[1] } # 1.8 -> 8
	if ($out -match 'version "(\d+)') { return [int]$Matches[1] }
	return $null
}

function Get-JavaHomeFromJavaExe {
	$javaCmd = Get-Command java -ErrorAction SilentlyContinue
	if (-not $javaCmd) { return $null }
	$javaExe = $javaCmd.Source
	$bin = Split-Path $javaExe -Parent
	return (Split-Path $bin -Parent)
}

function Install-Temurin21 {
	Write-Host "Java не найдена. Пробую установить Eclipse Temurin JDK 21 через winget..."
	$winget = Get-Command winget -ErrorAction SilentlyContinue
	if (-not $winget) {
		throw "winget не найден. Установите JDK 21 вручную или поставьте App Installer / winget."
	}

	& winget install -e --id EclipseAdoptium.Temurin.21.JDK --accept-package-agreements --accept-source-agreements --silent --force
	if ($LASTEXITCODE -ne 0) {
		throw "winget install завершился с ошибкой (код $LASTEXITCODE). Запустите PowerShell от имени администратора и повторите, либо установите JDK 21 вручную."
	}
	
	$machinePath = [Environment]::GetEnvironmentVariable("Path", "Machine")
	$userPath = [Environment]::GetEnvironmentVariable("Path", "User")
	$env:Path = "$machinePath;$userPath"
}

if (-not (Test-JavaAvailable)) {
	Install-Temurin21
}

if (-not (Test-JavaAvailable)) {
	throw "После установки Java всё ещё недоступна в PATH. Закройте окно PowerShell, откройте заново и запустите скрипт ещё раз."
}

$major = Get-JavaMajorVersion
if ($null -eq $major -or $major -lt 21) {
	Write-Host "Найдена Java, но версия не подходит для проекта (нужна 21+). Пробую поставить/обновить Temurin 21 через winget..."
	Install-Temurin21
	if (-not (Test-JavaAvailable)) {
		throw "После попытки установки JDK 21 Java всё ещё недоступна в PATH. Перезапустите PowerShell и попробуйте снова."
	}
	$major = Get-JavaMajorVersion
	if ($null -eq $major -or $major -lt 21) {
		throw "Java всё ещё не 21+. Проверьте, что в PATH первым идёт JDK 21 (команда: where.exe java)."
	}
}

$jh = Get-JavaHomeFromJavaExe
if (-not $jh) {
	throw "Не удалось определить JAVA_HOME автоматически."
}

$env:JAVA_HOME = $jh
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "JAVA_HOME=$env:JAVA_HOME"
Write-Host (& java -version 2>&1 | Out-String)

Set-Location $PSScriptRoot
Write-Host "Запуск: .\mvnw.cmd spring-boot:run"
& .\mvnw.cmd spring-boot:run
