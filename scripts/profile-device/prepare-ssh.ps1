# Does not modify the phone or the user's SSH config. Start the QEMU VM separately.
param(
    [string]$DockerApi = 'http://127.0.0.1:2375',
    [int]$Port = 20022,
    [string]$Container = 'termux-profile-ssh-test'
)
$ErrorActionPreference = 'Stop'
if (([uri]$DockerApi).Host -ne '127.0.0.1') { throw 'Only the loopback QEMU Docker API is allowed' }
$fixture = Join-Path (Resolve-Path "$PSScriptRoot/../..").Path 'build/profile-device'
New-Item -ItemType Directory -Force $fixture | Out-Null
$key = Join-Path $fixture 'id_ed25519'
if (!(Test-Path $key)) {
    & ssh-keygen -t ed25519 -N '' -C profile-device-test -f $key
    if ($LASTEXITCODE) { throw 'Key generation failed (PowerShell 7 required)' }
}
$public = (Get-Content "$key.pub" -Raw).Trim()
$containers = Invoke-RestMethod "$DockerApi/containers/json?all=true"
$existing = @($containers | Where-Object { $_.Names -contains "/$Container" })
if ($existing.Count) {
    if ($existing[0].Labels.'com.termux.profile-test' -ne 'true') { throw 'Container name belongs to another task' }
    $inspection = Invoke-RestMethod "$DockerApi/containers/$Container/json"
    if ($inspection.Config.Env -notcontains "TEST_PUBLIC_KEY=$public") { throw 'Existing fixture uses a different key; choose a new container/port' }
    if (!$inspection.State.Running) { Invoke-RestMethod -Method Post "$DockerApi/containers/$Container/start" | Out-Null }
} else {
    $pull = Invoke-WebRequest -Method Post "$DockerApi/images/create?fromImage=alpine&tag=3.22" -TimeoutSec 600
    if ($pull.Content -match '"error"\s*:') { throw $pull.Content }
    $init = 'apk add --no-cache openssh-server bash && ssh-keygen -A && mkdir -p /root/.ssh && printf "%s\n" "$TEST_PUBLIC_KEY" > /root/.ssh/authorized_keys && chmod 700 /root/.ssh && chmod 600 /root/.ssh/authorized_keys && exec /usr/sbin/sshd -D -e -o PasswordAuthentication=no -o PermitRootLogin=prohibit-password'
    # 0.0.0.0 is inside the isolated VM. QEMU forwards this only on host 127.0.0.1.
    $body = @{Image='alpine:3.22'; Cmd=@('/bin/sh','-ec',$init); Env=@("TEST_PUBLIC_KEY=$public");
        Labels=@{'com.termux.profile-test'='true'};
        HostConfig=@{PortBindings=@{'22/tcp'=@(@{HostIp='0.0.0.0';HostPort="$Port"})}}} | ConvertTo-Json -Depth 8
    Invoke-RestMethod -Method Post "$DockerApi/containers/create?name=$Container" -ContentType application/json -Body $body | Out-Null
    Invoke-RestMethod -Method Post "$DockerApi/containers/$Container/start" | Out-Null
}
Write-Output "SSH fixture: $Container; key: $key; host port: $Port"
Write-Output 'Wait for sshd readiness, then copy its ed25519 host public key via the trusted VM Docker CLI (see README).'
