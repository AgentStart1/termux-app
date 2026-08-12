package com.termux.app.profiles;

import android.content.Context;
import android.util.Log;

import com.termux.shared.shell.command.ExecutionCommand;
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession;

/**
 * Profile会话创建辅助类
 * 封装profile相关的session创建逻辑
 */
public class ProfileSessionHelper {

    private static final String TAG = "ProfileSessionHelper";

    /**
     * 根据Profile创建ExecutionCommand
     * @param profile Profile配置
     * @return ExecutionCommand实例
     */
    public static ExecutionCommand buildExecutionCommand(Profile profile) {
        ExecutionCommand command = new ExecutionCommand();

        if (profile == null) {
            Log.d(TAG, "Profile is null, using default settings");
            return command;
        }

        // 设置工作目录
        if (profile.getWorkingDirectory() != null && !profile.getWorkingDirectory().isEmpty()) {
            command.workingDirectory = profile.getWorkingDirectory();
        }

        // 根据Profile类型设置executable和arguments
        switch (profile.getType()) {
            case LOCAL:
                setupLocalProfile(command, profile);
                break;
            case PROOT:
                setupProotProfile(command, profile);
                break;
            case SSH:
                setupSshProfile(command, profile);
                break;
        }

        return command;
    }

    /**
     * 设置Local Profile的命令
     * 使用默认的shell检测逻辑
     */
    private static void setupLocalProfile(ExecutionCommand command, Profile profile) {
        // 如果指定了shell，使用指定的shell
        if (profile.getShell() != null && !profile.getShell().isEmpty()) {
            command.executable = profile.getShell();
        }
        // 否则保持executable为null，让TermuxSession使用默认的shell检测逻辑
    }

    /**
     * 设置Proot Profile的命令
     * 使用proot或proot-distro启动
     */
    private static void setupProotProfile(ExecutionCommand command, Profile profile) {
        String distro = profile.getProotDistro();
        String args = profile.getProotArgs();
        String startupCommand = profile.getProotCommand();

        // 检查是否安装了proot-distro
        // 如果有proot-distro，优先使用它
        // 否则使用proot

        // 使用proot-distro (推荐方式)
        command.executable = "proot-distro";

        java.util.List<String> argList = new java.util.ArrayList<>();
        argList.add("login");

        // 添加发行版
        if (distro != null && !distro.isEmpty()) {
            argList.add(distro);
        } else {
            argList.add("ubuntu"); // 默认使用ubuntu
        }

        // 添加额外参数
        if (args != null && !args.isEmpty()) {
            String[] extraArgs = args.split("\\s+");
            for (String arg : extraArgs) {
                if (!arg.isEmpty()) {
                    argList.add(arg);
                }
            }
        }

        // 添加启动命令
        if (startupCommand != null && !startupCommand.isEmpty()) {
            argList.add("--");
            argList.add("/bin/bash");
            argList.add("-c");
            argList.add(startupCommand);
        } else {
            // 默认启动bash
            argList.add("--");
            argList.add("/bin/bash");
        }

        command.arguments = argList.toArray(new String[0]);
    }

    /**
     * 设置SSH Profile的命令
     * 使用系统ssh命令
     */
    private static void setupSshProfile(ExecutionCommand command, Profile profile) {
        command.executable = "ssh";

        java.util.List<String> argList = new java.util.ArrayList<>();

        // 添加端口
        if (profile.getSshPort() != 22) {
            argList.add("-p");
            argList.add(String.valueOf(profile.getSshPort()));
        }

        // 添加密钥文件
        if (profile.getSshKeyFile() != null && !profile.getSshKeyFile().isEmpty()) {
            argList.add("-i");
            argList.add(profile.getSshKeyFile());
        }

        // 添加额外参数
        String sshArgs = profile.getSshArgs();
        if (sshArgs != null && !sshArgs.isEmpty()) {
            String[] extraArgs = sshArgs.split("\\s+");
            for (String arg : extraArgs) {
                if (!arg.isEmpty()) {
                    argList.add(arg);
                }
            }
        }

        // 添加用户@主机
        String user = profile.getSshUser();
        String host = profile.getSshHost();
        if (user != null && !user.isEmpty() && host != null && !host.isEmpty()) {
            argList.add(user + "@" + host);
        } else if (host != null && !host.isEmpty()) {
            argList.add(host);
        }

        command.arguments = argList.toArray(new String[0]);
    }

    /**
     * 获取Profile的显示名称
     * @param profile Profile
     * @return 显示名称
     */
    public static String getProfileDisplayName(Profile profile) {
        if (profile == null) {
            return "Unknown";
        }

        return profile.getName() + " (" + profile.getType().getDisplayName() + ")";
    }

    /**
     * 初始化ProfileSessionHelper
     * 在应用启动时调用
     * @param context Context
     */
    public static void init(Context context) {
        // 初始化ProfileManager，确保默认profile存在
        ProfileManager profileManager = ProfileManager.getInstance(context);
        Profile defaultProfile = profileManager.getDefaultProfile();

        Log.d(TAG, "ProfileSessionHelper initialized with default profile: " +
              (defaultProfile != null ? defaultProfile.getName() : "none"));
    }
}
