# 真实 Profile 后端检查

这是在专用 debug Termux 设备上执行的可选测试，要求 Android 8/API 26 或更新版本。它在应用 UID 下启动生产 `ProfileSessionHelper` 命令，使用真实 Termux、OpenSSH 和 Ubuntu Proot 进程，**不是**完整 UI 或 PTY 生命周期测试。Local Profile 创建和启动，以及三种基础连接的发现，已另行通过手机 UI 检查。

执行器对每个后端检查两次，覆盖扩展值按字面处理、保留现有 HOME、目标 home 目录、启动退出码 23、另一个引用相同基础连接的预设，以及无效目录短路。不写入真实预设。

## 一次性准备

1. 构建并安装 debug 应用，不卸载已有 Termux。签名不匹配时停止，不清除数据。正常初始化 bootstrap。
2. 准备手机时持有共享设备锁。在 **Termux 会话内部**运行 `prepare-termux.sh`。它安装软件包和专用 `codex-profile-ubuntu`（Ubuntu 24.04），不会重置已有容器。为依赖和客体预留约 1 GB。
3. 使用对应插件的 `scripts/start-vm.sh profiles/dev.profile` 启动已获准的持久 QEMU Alpine Docker VM。使用仅监听 loopback 的标准 20000–20255 转发，不得向局域网暴露 Docker API。
4. 用 PowerShell 7 运行 `prepare-ssh.ps1`。它创建或复用带标签的容器，并在被忽略的 `build/profile-device` 下生成仅供测试的密钥。不删除容器，不修改手机文件。等待 `run-docker.sh -- logs termux-profile-ssh-test` 显示 sshd 已监听。
5. 将 `id_ed25519` 复制到 Termux 的 `~/.ssh/codex-profile-test-key`，权限设为 600。通过 VM 插件的 `run-docker.sh -- exec termux-profile-ssh-test cat /etc/ssh/ssh_host_ed25519_key.pub` 获取可信公钥。在 `~/.ssh/codex-profile-test-known-hosts` 写入 `[127.0.0.1]:20022 <key-type> <key-data>`，权限设为 600。不要关闭主机密钥检查。
6. 手动将以下仅供测试的配置加入 `~/.ssh/config`（文件权限 600，目录 700），保留全部已有配置。不要复用已有别名：

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

## 可重复执行（PowerShell 7）

```powershell
./scripts/profile-device/run.ps1 -Serial DEVICE_SERIAL -DeviceLockScript /absolute/path/to/adb-device-lock.sh
```

脚本构建应用、单元测试和设备测试，获取共享设备锁，不清数据地更新 APK，缺少映射时添加 `adb reverse`，再执行 18 个真实后端用例。必须出现明确 PASS 标记才算成功，不能只看 adb 退出码为零。脚本在 `finally` 中只删除自己添加的 reverse 映射，并释放自己持有的锁；已有匹配映射予以保留。

Instrumentation 会重启应用进程，请先结束重要会话。VM、测试容器、Ubuntu 客体和生成密钥保留供后续运行。密钥放在被忽略的构建目录，绝不能提交。不再需要时，手动移除准确的测试别名、密钥、known-host 文件和专用夹具，不影响无关配置或容器。
