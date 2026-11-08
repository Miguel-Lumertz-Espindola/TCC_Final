param(
    [ValidateRange(1, 65535)][int]$Porta = 8080,
    [string]$AdbPath,
    [ValidateRange(0, 30)][int]$EsperaSegundos = 0,
    [switch]$DuranteBuild
)
$ErrorActionPreference = 'Stop'
try {
    if ([string]::IsNullOrWhiteSpace($AdbPath)) {
        $candidatos = @()
        $propriedades = Join-Path $PSScriptRoot 'local.properties'
        if (Test-Path -LiteralPath $propriedades) {
            $linha = Get-Content -LiteralPath $propriedades | Where-Object { $_ -match '^\s*sdk\.dir\s*=' } | Select-Object -First 1
            if ($linha) {
                $sdk = ($linha -replace '^\s*sdk\.dir\s*=\s*', '') -replace '\\:', ':' -replace '\\\\', '\'
                $candidatos += Join-Path $sdk 'platform-tools\adb.exe'
            }
        }
        foreach ($sdk in @($env:ANDROID_HOME, $env:ANDROID_SDK_ROOT)) {
            if ($sdk) { $candidatos += Join-Path $sdk 'platform-tools\adb.exe' }
        }
        if ($env:LOCALAPPDATA) { $candidatos += Join-Path $env:LOCALAPPDATA 'Android\Sdk\platform-tools\adb.exe' }
        $AdbPath = $candidatos | Where-Object { Test-Path -LiteralPath $_ } | Select-Object -First 1
        if (!$AdbPath) {
            $comando = Get-Command adb.exe -ErrorAction SilentlyContinue
            if ($comando) { $AdbPath = $comando.Source }
        }
    }
    if (!$AdbPath -or !(Test-Path -LiteralPath $AdbPath)) { throw 'ADB nao encontrado. Confira sdk.dir no arquivo local.properties do projeto.' }
    & $AdbPath start-server
    if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel iniciar o ADB.' }
    $limite = [DateTime]::UtcNow.AddSeconds($EsperaSegundos)
    do {
        $linhas = @(& $AdbPath devices)
        if ($LASTEXITCODE -ne 0) { throw 'Nao foi possivel listar os dispositivos ADB.' }
        $dispositivos = @($linhas | Where-Object { $_ -match '^\S+\s+device\s*$' } | ForEach-Object { ($_ -split '\s+')[0] })
        if ($dispositivos.Count -gt 0 -or [DateTime]::UtcNow -ge $limite) { break }
        Start-Sleep -Milliseconds 500
    } while ($true)
    if ($dispositivos.Count -eq 0) {
        throw 'Nenhum dispositivo pronto. Abra o emulador ou autorize a depuracao USB no celular e execute Run novamente.'
    }
    $falhas = 0
    foreach ($serial in $dispositivos) {
        & $AdbPath -s $serial reverse "tcp:$Porta" "tcp:$Porta" | Out-Null
        if ($LASTEXITCODE -ne 0) {
            $falhas++
            Write-Warning "Falha ao conectar $serial. Reconecte o dispositivo e execute Run novamente."
        } else {
            Write-Host "[API local] $serial conectado: 127.0.0.1:$Porta -> computador:$Porta"
        }
    }
    foreach ($linha in $linhas) {
        if ($linha -match '^\S+\s+(unauthorized|offline)\s*$') {
            Write-Warning "Dispositivo indisponivel: $linha. Confira a autorizacao USB ou aguarde a inicializacao."
        }
    }
    if ($falhas -gt 0) { throw 'Um ou mais dispositivos nao receberam o encaminhamento da API.' }
    $tcp = New-Object System.Net.Sockets.TcpClient
    try {
        $tentativa = $tcp.BeginConnect('127.0.0.1', $Porta, $null, $null)
        if (!$tentativa.AsyncWaitHandle.WaitOne(1000)) { throw 'Tempo de conexao esgotado.' }
        $tcp.EndConnect($tentativa)
    } catch {
        Write-Warning "O encaminhamento foi configurado, mas o backend nao esta acessivel na porta $Porta. Inicie o Spring Boot no IntelliJ."
    } finally { $tcp.Dispose() }
} catch {
    if ($DuranteBuild) { Write-Warning "[API local] $($_.Exception.Message)"; exit 0 }
    Write-Error $_.Exception.Message
    exit 1
}
