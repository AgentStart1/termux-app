# Termux 应用

[![Build status](https://github.com/termux/termux-app/workflows/Build/badge.svg)](https://github.com/termux/termux-app/actions)
[![Testing status](https://github.com/termux/termux-app/workflows/Unit%20tests/badge.svg)](https://github.com/termux/termux-app/actions)
[![Join the chat at https://gitter.im/termux/termux](https://badges.gitter.im/termux/termux.svg)](https://gitter.im/termux/termux)
[![Join the Termux discord server](https://img.shields.io/discord/641256914684084234.svg?label=&logo=discord&logoColor=ffffff&color=5865F2)](https://discord.gg/HXpF69X)
[![Termux library releases at Jitpack](https://jitpack.io/v/termux/termux-app.svg)](https://jitpack.io/#termux/termux-app)



[Termux](https://termux.dev) 是 Android 终端应用和 Linux 环境。

本仓库维护应用本身，包括用户界面和终端模拟。应用内可安装的软件包见 [termux/termux-packages](https://github.com/termux/termux-packages)。

Termux 包管理的简要说明见 [Package Management](https://github.com/termux/termux-packages/wiki/Package-Management)，其中也说明了运行 `apt` 或 `pkg` 时，如何处理 **`repository is under maintenance or down`** 错误。

**我们正在寻找 Termux Android 应用维护者。**

***

**注意：Termux 在 Android 12+ 上可能不稳定。** Android 会终止超过 32 个的 phantom 进程（所有应用合计的上限），也会终止 CPU 使用过高的进程。即使没有主动退出 shell，也可能在终端看到 `[Process completed (signal 9) - press Enter]`。有关关闭 phantom 进程和高 CPU 进程裁剪的方法，参见相关 [#2366](https://github.com/termux/termux-app/issues/2366)、[问题跟踪](https://issuetracker.google.com/u/1/issues/205156966)、[phantom、cached 和 empty 进程文档](https://github.com/agnostic-apollo/Android-Docs/blob/master/en/docs/apps/processes/phantom-cached-and-empty-processes.md)，以及[简要说明](https://github.com/termux/termux-app/issues/2366#issuecomment-1237468220)。后续会补充正式文档页。Android 12L 或 13 应提供关闭此终止机制的选项；仍在 Android 11，尤其未 root 的用户，请自行评估升级风险。

***

## 目录

- [Termux 应用与插件](#termux-应用与插件)
- [安装](#安装)
- [卸载](#卸载)
- [重要链接](#重要链接)
- [调试](#调试)
- [维护者与贡献者](#维护者与贡献者)
- [创建 Fork](#创建-fork)
- [赞助方与资助方](#赞助方与资助方)
##

## Termux 应用与插件

核心 [Termux](https://github.com/termux/termux-app) 应用可配合以下可选插件应用使用：

- [Termux:API](https://github.com/termux/termux-api)
- [Termux:Boot](https://github.com/termux/termux-boot)
- [Termux:Float](https://github.com/termux/termux-float)
- [Termux:Styling](https://github.com/termux/termux-styling)
- [Termux:Tasker](https://github.com/termux/termux-tasker)
- [Termux:Widget](https://github.com/termux/termux-widget)
##

## 安装

最新版本为 `v0.118.3`。

**注意：强烈建议尽快更新到 `v0.118.0` 或更新版本，以获得多项缺陷修复，包括[此处披露](https://termux.github.io/general/2022/02/15/termux-apps-vulnerability-disclosures.html)的严重全局可读漏洞。Google Play 上的 Termux 信息见[下文](#google-play-store-实验分支)。**

可通过下列来源获取 Termux；只有 Android `>= 7` 获得应用和软件包的完整支持。

Android `5` 和 `6` 的应用及软件包支持在 [2020-01-01](https://www.reddit.com/r/termux/comments/dnzdbs/end_of_android56_support_on_20200101/) 的 `v0.83` 中终止，随后在 [2022-05-24](https://github.com/termux/termux-app/pull/2740) 通过 [GitHub](#github) 来源恢复了仅应用支持，*不支持软件包更新*。详情见[这里](https://github.com/termux/termux-app/wiki/Termux-on-android-5-or-6)。

不同来源的 APK 使用不同签名密钥。`Termux` 应用和全部插件共用 [`sharedUserId`](https://developer.android.com/guide/topics/manifest/manifest-element) `com.termux`，因此设备上的所有相关 APK 必须使用同一密钥签名，才能协同工作，也就必须从同一来源安装。不要混用来源，例如一个应用或插件来自 `F-Droid`，另一个来自 `GitHub`。Android 包管理器通常也不允许安装签名不同的 APK，会提示 `App not installed`、`Failed to install due to an unknown error`、`INSTALL_FAILED_UPDATE_INCOMPATIBLE`、`INSTALL_FAILED_SHARED_USER_INCOMPATIBLE` 或 `signatures do not match previously installed version` 等错误。Root 或自定义 ROM 可绕过此限制。

若要改用其他来源，必须先从设备**卸载全部已有 Termux 和插件应用 APK**，再从同一个新来源安装所有 APK。详情见[卸载](#卸载)。卸载前也可考虑[备份 Termux](https://wiki.termux.dev/wiki/Backing_up_Termux)，以便从其他来源重新安装后恢复。

下文的 *bootstrap* 指 `termux-app` 自带、用于启动可用 shell 环境的最小软件包集合。其 ZIP 在[此处](https://github.com/termux/termux-packages/releases)构建和发布。

### F-Droid

可从 [F-Droid](https://f-droid.org/en/packages/com.termux/) 获取 Termux 应用。

安装 Termux **不需要**通过 `Download F-Droid` 下载 F-Droid 应用。可点击每个版本区域底部的 `Download APK`，直接从网站下载 Termux APK。

`GitHub` 发布更新后，通常需要几天，甚至一周或更久，才会在 `F-Droid` 提供。`F-Droid` [检测到](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.termux.yml)新的 GitHub 版本后，自行构建和发布。Termux 维护者**无法控制** F-Droid 上 Termux 应用的构建和发布，也无法访问 F-Droid 发布版的 APK 签名密钥，因此不能自行在 GitHub 发布与 F-Droid 版本兼容的 APK。

F-Droid 应用可能不主动通知更新，需要在 `Updates` 标签页手动下拉刷新，才能检查更新。请确保对它关闭电池优化；操作方法见 https://dontkillmyapp.com/。

只发布通用 APK，适用于全部支持的架构。APK 和 bootstrap 安装大小约为 `~180MB`。F-Droid [不支持](https://github.com/termux/termux-app/pull/1904)按架构发布 APK。

### GitHub

GitHub 上可通过 [`GitHub Releases`](https://github.com/termux/termux-app/releases) 获取 `>= 0.118.0` 的版本，也可通过 [`GitHub Build Action`](https://github.com/termux/termux-app/actions/workflows/debug_build.yml?query=branch%3Amaster+event%3Apush) 工作流获取。**Android `>= 7` 只安装 `apt-android-7` 变体；Android `5` 和 `6` 只安装 `apt-android-5` 变体。**

`GitHub Releases` 的 APK 位于对应版本的 `Assets` 下拉区，新版本发布时自动附加。

`GitHub Build` 工作流的 APK 位于运行页的 `Artifacts` 区。仓库每次提交或推送都会生成，适合不想等待正式发布、想立即试用最新功能，或测试自己 pull request 的用户。工作流的 artifact 链接只有在[**登录 GitHub 账户**](https://github.com/login)后才可点击。如果使用 [`GitHub` 应用](https://github.com/mobile)，请用已登录 GitHub 的 Chrome 或 Firefox 等浏览器打开工作流链接，因为应用内浏览器可能未登录。

这两种 APK 均为 [`debuggable`](https://developer.android.com/studio/debug)，彼此兼容，但与其他来源不兼容。

同时发布通用 APK 和架构专用 APK。通用 APK 加 bootstrap 安装大小约 `~180MB`，架构专用版本约 `~120MB`，详情见[这里](https://github.com/termux/termux-app/issues/2153)。

**安全警告**：GitHub APK 使用[向社区公开](https://github.com/termux/termux-app/blob/master/app/testkey_untrusted.jks)的测试密钥签名。它不是官方开发者密钥，任何人都能用它生成自己的测试版本。使用并非来自 https://github.com/termux/termux-app 的 GitHub 构建时必须谨慎。任何人都可以用该密钥伪造恶意更新，并覆盖安装到 GitHub 构建上。安装通过 Telegram 或其他社交媒体分发的 Termux 构建前，请仔细考虑。如果设备感染恶意软件，我们无法提供帮助。

[测试密钥](https://github.com/termux/termux-app/blob/master/app/testkey_untrusted.jks) 不得用于冒充 @termux，它也无法实现这一目的。我们不信任此密钥，在用户生成内容中识别其使用也很容易。

<details>
<summary>密钥库信息</summary>

```
Alias name: alias
Creation date: Oct 4, 2019
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=APK Signer, OU=Earth, O=Earth
Issuer: CN=APK Signer, OU=Earth, O=Earth
Serial number: 29be297b
Valid from: Wed Sep 04 02:03:24 EEST 2019 until: Tue Oct 26 02:03:24 EEST 2049
Certificate fingerprints:
         SHA1: 51:79:55:EA:BF:69:FC:05:7C:41:C7:D3:79:DB:BC:EF:20:AD:85:F2
         SHA256: B6:DA:01:48:0E:EF:D5:FB:F2:CD:37:71:B8:D1:02:1E:C7:91:30:4B:DD:6C:4B:F4:1D:3F:AA:BA:D4:8E:E5:E1
Signature algorithm name: SHA1withRSA (disabled)
Subject Public Key Algorithm: 2048-bit RSA key
Version: 3
```

</details>

### Google Play Store **（实验分支）**

Google Play 当前为 Android 11+ 设备提供 Termux 构建，为通过其政策要求进行了大量调整。此版本仍在开发，与稳定的 F-Droid 构建相比，存在功能缺失和缺陷（状态更新见[这里](https://github.com/termux-play-store/)），因此大多数具备条件的用户仍应使用前述 F-Droid 或 GitHub 构建。

Google Play 当前会尝试更新 F-Droid 安装，但由于移除了 [sharedUserId](https://developer.android.com/guide/topics/manifest/manifest-element#uid)，更新仍会失败。计划中的 0.118.1 F-Droid 发布会使用比 PlayStore 应用更高的 version code 来修复此问题。在此之前，为避免 Google Play 尝试下载并覆盖现有安装后失败，可以打开 Google Play 的 Termux 应用页，点击右上角三点菜单，关闭 Enable auto update。不过 Termux 更新仍会显示在 PlayStore 应用更新列表中。

如果希望帮助测试 Google Play 构建，或无法从其他来源安装 Termux，请注意它来自[独立仓库](https://github.com/termux-play-store/)。遇到的问题很可能仅属于该仓库，请在[那里](https://github.com/termux-play-store/termux-issues/issues/new/choose)反馈。

## 卸载

不再希望设备安装 Termux，或准备切换[安装来源](#安装)时，可能需要卸载。卸载前也可考虑[备份 Termux](https://wiki.termux.com/wiki/Backing_up_Termux)。

完整卸载 Termux，必须卸载 [Termux 应用与插件](#termux-应用与插件) 中列出的**全部已有 Termux 和插件应用 APK**。

打开 `Android Settings` → `Applications`，找到这些应用。设备支持搜索时，也可在应用列表搜索 `termux`。

即使认为自己没有安装插件，也强烈建议在 Android 设置的应用列表中逐一确认。
##

## 重要链接

### 社区

全部社区链接见[这里](https://wiki.termux.com/wiki/Community)。主要链接如下：

- [Termux Reddit 社区](https://reddit.com/r/termux)
- [Termux 用户 Matrix 频道](https://matrix.to/#/#termux_termux:gitter.im)（[Gitter](https://gitter.im/termux/termux)）
- [Termux 开发者 Matrix 频道](https://matrix.to/#/#termux_dev:gitter.im)（[Gitter](https://gitter.im/termux/dev)）
- [Termux X (Twitter)](https://twitter.com/termuxdevs)
- [Termux 支持邮箱](mailto:support@termux.dev)

### Wiki

- [Termux Wiki](https://wiki.termux.com/wiki/)
- [Termux App Wiki](https://github.com/termux/termux-app/wiki)
- [Termux Packages Wiki](https://github.com/termux/termux-packages/wiki)

### 其他资料

- [常见问题](https://wiki.termux.com/wiki/FAQ)
- [Termux 文件系统布局](https://github.com/termux/termux-packages/wiki/Termux-file-system-layout)
- [与 Linux 的差异](https://wiki.termux.com/wiki/Differences_from_Linux)
- [包管理](https://wiki.termux.com/wiki/Package_Management)
- [远程访问](https://wiki.termux.com/wiki/Remote_Access)
- [备份 Termux](https://wiki.termux.com/wiki/Backing_up_Termux)
- [终端设置](https://wiki.termux.com/wiki/Terminal_Settings)
- [触摸键盘](https://wiki.termux.com/wiki/Touch_Keyboard)
- [Android 存储及与其他应用共享数据](https://wiki.termux.com/wiki/Internal_and_external_storage)
- [Android API](https://wiki.termux.com/wiki/Termux:API)
- [Termux 软件包托管从 Bintray 迁移到 IPFS](https://github.com/termux/termux-packages/issues/6348)
- [其他应用通过 `RUN_COMMAND` intent 在 Termux 中运行命令](https://github.com/termux/termux-app/wiki/RUN_COMMAND-Intent)
- [Termux 与 Android 10](https://github.com/termux/termux-packages/wiki/Termux-and-Android-10)

### 终端

<details>
<summary></summary>

### 终端资料

- [XTerm 控制序列](https://invisible-island.net/xterm/ctlseqs/ctlseqs.html)
- [vt100.net](https://vt100.net/)
- [终端代码：ANSI 与 terminfo 对应关系](https://wiki.bash-hackers.org/scripting/terminalcodes)

### 终端模拟器

- VTE（libvte）：GTK+ 的终端模拟器控件，主要用于 gnome-terminal。[源码](https://github.com/GNOME/vte)、[未关闭问题](https://bugzilla.gnome.org/buglist.cgi?quicksearch=product%3A%22vte%22+)、[全部问题（含已关闭）](https://bugzilla.gnome.org/buglist.cgi?bug_status=RESOLVED&bug_status=VERIFIED&chfield=resolution&chfieldfrom=-2000d&chfieldvalue=FIXED&product=vte&resolution=FIXED)。
- iTerm 2：OS X 终端应用。[源码](https://github.com/gnachman/iTerm2)、[问题](https://gitlab.com/gnachman/iterm2/issues)和[文档](https://iterm2.com/documentation.html)，其中包含 [iTerm2 专用转义代码](https://iterm2.com/documentation-escape-codes.html)。
- Konsole：KDE 终端应用。[源码](https://projects.kde.org/projects/kde/applications/konsole/repository)，尤其是[测试](https://projects.kde.org/projects/kde/applications/konsole/repository/revisions/master/show/tests)、[缺陷](https://bugs.kde.org/buglist.cgi?bug_severity=critical&bug_severity=grave&bug_severity=major&bug_severity=crash&bug_severity=normal&bug_severity=minor&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&product=konsole)和[功能愿望](https://bugs.kde.org/buglist.cgi?bug_severity=wishlist&bug_status=UNCONFIRMED&bug_status=NEW&bug_status=ASSIGNED&bug_status=REOPENED&product=konsole)。
- hterm：来自 Chromium 的 JavaScript 终端实现。[源码](https://github.com/chromium/hterm)，包括[测试](https://github.com/chromium/hterm/blob/master/js/hterm_vt_tests.js)，以及 [Google 讨论组](https://groups.google.com/a/chromium.org/forum/#!forum/chromium-hterm)。
- xterm：终端模拟器的鼻祖。[源码](https://invisible-island.net/datafiles/release/xterm.tar.gz)。
- Connectbot：Android SSH 客户端。[源码](https://github.com/connectbot/connectbot)。
- Android Terminal Emulator：Termux 终端处理的基础，现已停止活跃开发。[源码](https://github.com/jackpal/Android-Terminal-Emulator)。
</details>

##

### 调试

可在 Termux 应用设置 → `<APP_NAME>` → `Debugging` → `Log Level` 中设置合适的 logcat 日志级别，帮助调试 Termux 应用及插件（要求 Termux `>= 0.118.0`）。默认 `Log Level` 为 `Normal`；`Verbose` 当前会记录更多信息。完成调试后，最好恢复为 `Normal`，否则正常操作中的私有数据可能传入 logcat，额外日志也会增加执行耗时。

插件应用**不自行执行命令**，而是向 Termux 应用发送执行 intent。Termux 自身有独立日志级别，可在 Termux 应用设置 → `Termux` → `Debugging` → `Log Level` 中配置。因此必须同时为 Termux 和对应插件设置日志级别，才能获得完整信息。

设置日志级别后，在 Termux 终端运行 `logcat` 可实时查看日志（`Ctrl+c` 停止），也可用 `logcat -d > logcat.txt` 导出日志。还可在 PC 上通过 ADB 查看。更多信息见 [Android 官方 logcat 指南](https://developer.android.com/studio/command-line/logcat)。

还可通过终端长按菜单 `More` → `Report Issue`，在添加调试信息的提示中选择 `YES`，自动生成 Termux 文件 stat 信息和 logcat 导出。这也有助于报告和调试其他问题。报告过大时，可在 `ReportActivity` 右上角三点菜单中选择 `Save To File`，再查看或分享文件。

报告问题时必须提供完整报告，可移除敏感信息。只提供错误报告的部分截图、而非文本的问题，很可能被自动关闭或删除。

##### 日志级别

- `Off`：不记录日志。
- `Normal`：记录 error、warn、info 消息和堆栈。
- `Debug`：进一步记录 debug 消息。
- `Verbose`：进一步记录 verbose 消息。
##

## 维护者与贡献者

### Profile 线程模型

Profile 是有名称的预设，引用只读的 Local、SSH Host 别名或已安装 Proot 连接。多个预设可引用同一连接。只允许编辑目标工作目录、补充环境变量和启动命令。保留已存在的环境变量，包括空值。指定启动命令后，会话以其退出状态结束；不指定则打开交互 shell。目录错误会终止启动，不静默回退。

SSH 选项留在 `~/.ssh/config`，应用不写入该文件。发现过程只列出文件中明确的 Host 别名，不列出通配模式或仅通过 Include 引入的别名。Proot 发现读取 installed-rootfs 和 containers/rootfs 布局，不读取可安装发行版目录。刷新或启动时重新检查可用性；连接缺失时保留预设供重新绑定。新模型使用 `presets-v2.json`；旧数据不迁移、不加载，也不删除。

目标侧扩展要求 SSH/Proot 目标提供 POSIX `/bin/sh`。SSH 不覆盖 RequestTTY 或其他配置选项；交互远程程序所需设置应在 Host 中配置。环境值和路径按字面值处理，启动命令则明确作为 shell 代码执行。

Profile UI 必须使用 `ProfileManager` 上与生命周期绑定的异步方法。`ProfileHost` 初始化存储，并在应用管理的后台队列上串行处理加载、修改、持久化和发行版检测。UI 回调不要调用 `ProfileRepository` 或 `ProfileStorageManager`。快照和编辑器值都是独立副本；修改编辑器不会改变已存状态，只有显式保存才生效。结果只交付给仍活动的生命周期管理者，管理者销毁时解除关联。已入队写入由应用管理，即使发起页面关闭仍会完成。

确定性的宿主测试命令为 `./gradlew :app:testDebugUnitTest --tests com.termux.app.profiles.ProfileHostTest`。

使用 QEMU Alpine 中真实 SSH Docker 服务和 Ubuntu Proot 客体的可选实体设备检查，见 [Profile 设备测试](scripts/profile-device/README.md)。PowerShell 执行器持有共享设备锁，并保留已有应用数据。

[termux-shared](termux-shared) 库在 [`v0.109`](https://github.com/termux/termux-app/releases/tag/v0.109) 加入，定义 Termux 及插件共用的常量和工具，目的是消除 Termux 应用中的全部硬编码路径。部分插件已使用该库，其余未来也会使用。贡献代码需要可能共用的常量或工具时，若尚不存在，应在 termux-shared 中定义并引用，同时更新相关 changelog。使用硬编码值的 pull request **不会／不应被接受**。Termux 应用和插件专用类应放在 `com.termux.shared.termux` 包中，通用类放在其外。贡献代码时还必须检查 [`termux-shared` 的 `LICENSE`](termux-shared/LICENSE.md)，必要时更新。必须尊重任何外部库或代码的许可证。

主要 Termux 常量由 [`TermuxConstants`](https://github.com/termux/termux-app/blob/master/termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java) 定义，也包含 fork Termux 或使用自定义包名构建的说明。修改包名需要使用新的 `$PREFIX` 构建 bootstrap ZIP 和其他软件包，更多信息见[构建软件包](https://github.com/termux/termux-packages/wiki/Building-packages)。

在插件应用中导入 Termux 库的方法见 [Termux Libraries](https://github.com/termux/termux-app/wiki/Termux-Libraries)；更新插件所用 Termux 库的方法见 [Forking and Local Development](https://github.com/termux/termux-app/wiki/Termux-Libraries#forking-and-local-development)。

Termux 及插件 `build.gradle` 中的 `versionName` 必须遵循[语义化版本 2.0.0](https://semver.org/spec/v2.0.0.html)，格式为 `major.minor.patch(-prerelease)(+buildmetadata)`。增加 versionName 或为 GitHub 新版本创建 tag 时，必须包含 patch 数字，例如 `v0.1.0`，而非 `v0.1`。build.gradle 和 `attach_debug_apks_to_release` 工作流也会验证版本，不符合规范时构建或附件上传将失败。

### 提交信息指南

提交信息**必须**遵循 [Conventional Commits](https://www.conventionalcommits.org)，以便 [`create-conventional-changelog`](https://github.com/termux/create-conventional-changelog) 自动生成符合 [Keep a Changelog](https://github.com/olivierlacan/keep-a-changelog) 的变更日志；更多规范细节见其仓库。**`type` 和 `description` 首字母必须大写，描述应使用现在时。** 冒号 `:` 后必须有空格。破坏性变更在冒号前添加 `!`，使其在变更日志中自动突出显示。

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**只允许使用下面列出的类型，并与 changelog 标题中的写法完全一致。** 例如 `Added: Add foo`、`Added|Fixed: Add foo and fix bar`、`Changed!: Change baz as a breaking change`。也可加可选 scope，例如 `Fixed(terminal): Fix some bug`。**不要使用其他 type，例如不能用 `add` 代替 `Added`。**

- **Added**：新增功能。
- **Changed**：修改已有功能。
- **Deprecated**：即将移除的功能。
- **Removed**：已经移除的功能。
- **Fixed**：缺陷修复。
- **Security**：安全漏洞相关变更。
##

## 创建 Fork

- 修改应用包名需要的变更，见 [`TermuxConstants`](https://github.com/termux/termux-app/blob/master/termux-shared/src/main/java/com/termux/shared/termux/TermuxConstants.java) 的 Javadoc。
- 还必须为新包名重新编译 bootstrap ZIP，参见[构建 bootstrap](https://github.com/termux/termux-packages/wiki/For-maintainers#build-bootstrap-archives)、[此问题](https://github.com/termux/termux-app/issues/1983)和[此评论](https://github.com/termux/termux-app/issues/2081#issuecomment-865280111)。
- 当前并非全部插件都使用 termux-shared 的 TermuxConstants，部分仍硬编码 `com.termux`，需要手动修补。
- Fork Termux 插件时，使用 Termux 库的方法见 [Forking and Local Development](https://github.com/termux/termux-app/wiki/Termux-Libraries#forking-and-local-development)。
##

## 赞助方与资助方

[<img alt="GitHub Accelerator" width="25%" src="site/assets/sponsors/github.png" />](https://github.com)  
*[GitHub Accelerator](https://github.com/accelerator) ([1](https://github.blog/2023-04-12-github-accelerator-our-first-cohort-and-whats-next))*

&nbsp;

[<img alt="GitHub Secure Open Source Fund" width="25%" src="site/assets/sponsors/github.png" />](https://github.com)  
*[GitHub Secure Open Source Fund](https://resources.github.com/github-secure-open-source-fund) ([1](https://github.blog/open-source/maintainers/securing-the-supply-chain-at-scale-starting-with-71-important-open-source-projects), [2](https://termux.dev/en/posts/general/2025/08/11/termux-selected-for-github-secure-open-source-fund-session-2.html))*

&nbsp;

[<img alt="NLnet NGI Mobifree" width="25%" src="site/assets/sponsors/nlnet-ngi-mobifree.png" />](https://nlnet.nl/mobifree)  
*[NLnet NGI Mobifree](https://nlnet.nl/mobifree) ([1](https://nlnet.nl/news/2024/20241111-NGI-Mobifree-grants.html), [2](https://termux.dev/en/posts/general/2024/11/11/termux-selected-for-nlnet-ngi-mobifree-grant.html))*

&nbsp;

[<img alt="Cloudflare" width="25%" src="site/assets/sponsors/cloudflare.png" />](https://www.cloudflare.com)  
*[Cloudflare](https://www.cloudflare.com) ([1](https://packages-cf.termux.dev))*
