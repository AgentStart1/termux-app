# Requires PowerShell 7, adb, Git Bash and the shared device-lock plugin.
param(
    [Parameter(Mandatory)][string]$Serial,
    [Parameter(Mandatory)][string]$DeviceLockScript,
    [string]$Bash = 'C:/Program Files/Git/bin/bash.exe',
    [string]$SshAlias = 'codex-profile-ssh',
    [string]$ProotName = 'codex-profile-ubuntu',
    [int]$SshPort = 20022,
    [switch]$SkipBuild
)
$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path "$PSScriptRoot/../..").Path
$token = Join-Path ([IO.Path]::GetTempPath()) ("profile-lock-" + [guid]::NewGuid() + '.token')
$oldConversion = $env:MSYS2_ARG_CONV_EXCL
$env:MSYS2_ARG_CONV_EXCL = '/data/local/tmp/appium-device-test.lock.d/lock.json'
$locked = $false
$reverseCreated = $false
function Check-Exit([string]$operation) { if ($LASTEXITCODE -ne 0) { throw "$operation failed ($LASTEXITCODE)" } }
Push-Location $repo
try {
    if (!$SkipBuild) {
        & ./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest --no-daemon
        Check-Exit 'Build'
    }
    & $Bash $DeviceLockScript acquire --serial $Serial --project-dir $repo --test-name profile-real-backends --max-timeout-seconds 900 --wait-timeout-seconds 60 --token-file $token
    Check-Exit 'Device lock'; $locked = $true
    # Never uninstall on signature mismatch: adb install -r must preserve app data.
    $abi = (& adb -s $Serial shell getprop ro.product.cpu.abi).Trim()
    & adb -s $Serial install -r "app/build/outputs/apk/debug/termux-app_apt-android-7-debug_$abi.apk"
    Check-Exit 'App installation'
    & adb -s $Serial install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
    Check-Exit 'Test installation'
    $reverses = & adb -s $Serial reverse --list
    Check-Exit 'Read reverse mappings'
    $existing = @($reverses | Where-Object { $_ -match "\stcp:$SshPort\s" })
    if ($existing.Count) {
        if ($existing[0] -notmatch "\stcp:$SshPort\s+tcp:$SshPort$") { throw 'Port already mapped to a different destination' }
    } else {
        & adb -s $Serial reverse "tcp:$SshPort" "tcp:$SshPort"
        Check-Exit 'SSH reverse mapping'; $reverseCreated = $true
    }
    $output = & adb -s $Serial shell am instrument -w -e sshAlias $SshAlias -e prootName $ProotName com.termux.test/com.termux.app.profiles.ProfileDeviceRunner
    Check-Exit 'Instrumentation'
    $output | Write-Output
    # am instrument may exit zero even when the test runner reports failure.
    if (($output -join "`n") -notmatch 'PROFILE_DEVICE_TESTS_PASSED=18' -or
        ($output -join "`n") -match 'PROFILE_DEVICE_TESTS_FAILED') { throw 'Real backend checks failed' }
} finally {
    if ($reverseCreated) { & adb -s $Serial reverse --remove "tcp:$SshPort" }
    if ($locked) { & $Bash $DeviceLockScript release --serial $Serial --token-file $token }
    $env:MSYS2_ARG_CONV_EXCL = $oldConversion
    Pop-Location
}
