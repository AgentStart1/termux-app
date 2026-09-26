#!/data/data/com.termux/files/usr/bin/bash
# Run inside a Termux app session (not adb run-as: its DNS context differs).
set -euo pipefail
name=codex-profile-ubuntu
apt-get update
apt-get install -y openssh proot-distro
if [ ! -d "$PREFIX/var/lib/proot-distro/containers/$name/rootfs" ]; then
    proot-distro install --name "$name" ubuntu:24.04
fi
proot-distro login "$name" -- /bin/sh -c 'test -f /etc/os-release && cat /etc/os-release'
