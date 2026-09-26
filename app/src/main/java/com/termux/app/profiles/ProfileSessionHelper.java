package com.termux.app.profiles;
import android.content.Context;
import com.termux.shared.shell.command.ExecutionCommand;
import com.termux.shared.termux.TermuxConstants;

public final class ProfileSessionHelper {
    public static ExecutionCommand buildExecutionCommand(Profile profile) {
        if (profile == null || !profile.isValid()) throw new IllegalArgumentException("Invalid profile");
        if (!profile.isAvailable()) throw new IllegalArgumentException("Base connection unavailable; edit to rebind");
        ExecutionCommand command = new ExecutionCommand();
        boolean extensions = !profile.getWorkingDirectory().isEmpty() ||
            !profile.getStartupCommand().isEmpty() || !profile.getEnvironmentVariables().isEmpty();
        String script = extensions ? extensionScript(profile) : null;
        switch (profile.getType()) {
            case LOCAL:
                if (extensions) {
                    command.executable = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/sh";
                    command.arguments = new String[]{"-c", script};
                }
                break;
            case SSH:
                command.executable = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/ssh";
                command.arguments = extensions
                    ? new String[]{profile.getBaseId(), "/bin/sh -c " + quote(script)}
                    : new String[]{profile.getBaseId()};
                break;
            case PROOT:
                command.executable = TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/proot-distro";
                command.arguments = extensions
                    ? new String[]{"login", profile.getBaseId(), "--", "/bin/sh", "-c", script}
                    : new String[]{"login", profile.getBaseId()};
                break;
        }
        return command;
    }
    static String quote(String value) { return "'" + value.replace("'", "'\\''") + "'"; }
    static String extensionScript(Profile profile) {
        if (!profile.isValid()) throw new IllegalArgumentException("Invalid profile");
        StringBuilder script = new StringBuilder();
        for (java.util.Map.Entry<String, String> entry : profile.getEnvironmentVariables().entrySet()) {
            String key = entry.getKey();
            script.append("if [ \"$").append("{").append(key).append("+x}\" != x ]; then export ")
                .append(key).append("=").append(quote(entry.getValue())).append("; fi\n");
        }
        String directory = profile.getWorkingDirectory();
        if (!directory.isEmpty()) {
            String target = directory.equals("~") ? "\"$HOME\"" :
                directory.startsWith("~/") ? "\"$HOME\"/" + quote(directory.substring(2)) : quote(directory);
            script.append("CDPATH= cd -- ").append(target).append(" || exit $?\n");
        }
        String shell = "\"$" + "{SHELL:-/bin/sh}\"";
        if (!profile.getStartupCommand().isEmpty()) {
            script.append("exec ").append(shell).append(" -c ").append(quote(profile.getStartupCommand())).append("\n");
        } else {
            script.append("exec ").append(shell).append(" -i\n");
        }
        return script.toString();
    }
    public static String getProfileDisplayName(Profile profile) { return profile == null ? "Unknown" : profile.getName(); }
    public static void init(Context context) { ProfileManager.getInstance(context).initialize(); }
}
