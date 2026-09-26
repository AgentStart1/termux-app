# Real Profile backend checks

Opt-in tests on a dedicated debug Termux device, Android 8/API 26 or newer.
They launch the production `ProfileSessionHelper` commands inside the app UID,
using real Termux, OpenSSH and Ubuntu Proot processes. They are **not** a full UI
or PTY lifecycle test. Local Profile creation/launch and discovery of all three
bases were separately checked through the phone UI.

The runner checks each backend twice: literal extension values, existing HOME
preserved, target-home directory, startup exit code 23, another preset referencing
the same base, and invalid-directory short-circuit. No real presets are written.

## One-time setup

1. Build/install the debug app without uninstalling existing Termux. Stop on
   signature mismatch; do not clear data. Initialize its bootstrap normally.
2. Hold the shared device lock while preparing the phone. Run
   `prepare-termux.sh` **inside a Termux session**. It installs packages and a
   dedicated `codex-profile-ubuntu` (Ubuntu 24.04); it never resets an existing
   container. Reserve roughly 1 GB for dependencies and the guest.
3. Start the approved persistent QEMU Alpine Docker VM using its plugin's
   `scripts/start-vm.sh profiles/dev.profile`. Use the standard loopback-only
   20000–20255 forwards. Never expose the Docker API to the LAN.
4. Run `prepare-ssh.ps1` with PowerShell 7. It creates/reuses a labelled container
   and a generated test-only key under ignored `build/profile-device`. It does
   not delete containers or modify phone files. Wait for `sshd` listening in
   `run-docker.sh -- logs termux-profile-ssh-test`.
5. Copy `id_ed25519` into Termux `~/.ssh/codex-profile-test-key` (mode 600).
   Obtain the trusted public host key with the VM plugin's
   `run-docker.sh -- exec termux-profile-ssh-test cat /etc/ssh/ssh_host_ed25519_key.pub`.
   Put `[127.0.0.1]:20022 <key-type> <key-data>` into
   `~/.ssh/codex-profile-test-known-hosts` (mode 600). Do not disable host-key checking.
6. Manually add this test-only block to `~/.ssh/config` (mode 600; directory 700),
   preserving all existing configuration. Do not reuse a pre-existing alias:

```sshconfig
Host codex-profile-ssh
    HostName 127.0.0.1
    Port 20022
    User root
    IdentityFile ~/.ssh/codex-profile-test-key
    IdentitiesOnly yes
    BatchMode yes
    StrictHostKeyChecking yes
    UserKnownHostsFile ~/.ssh/codex-profile-test-known-hosts
    RequestTTY force
```

## Repeatable run (PowerShell 7)

```powershell
./scripts/profile-device/run.ps1 -Serial DEVICE_SERIAL -DeviceLockScript /absolute/path/to/adb-device-lock.sh
```

The script builds app/unit/device tests, acquires the shared device lock, updates
APKs without clearing data, adds an `adb reverse` mapping if missing, and executes
18 real-backend cases. Success requires the explicit PASS marker, not merely
`adb` exit code zero. It removes only its own reverse mapping and releases its
owned lock in `finally`. Existing matching mappings are preserved.

Instrumentation restarts the app process; finish important sessions first. The
VM, test container, Ubuntu guest and generated keys remain for subsequent runs.
Keep keys under ignored build directories; never commit them. Remove the exact
test alias/key/known-host file and dedicated fixtures manually when no longer
needed, without touching unrelated configuration or containers.
