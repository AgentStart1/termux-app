package com.termux.app.profiles;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;
import com.termux.shared.shell.command.ExecutionCommand;
import com.termux.shared.termux.TermuxConstants;
import com.termux.shared.termux.shell.command.environment.TermuxShellEnvironment;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Opt-in real backend checks. Requires the isolated fixtures described in scripts/profile-device. */
public final class ProfileDeviceRunner extends Instrumentation {
    private Bundle arguments;
    private int passed;
    @Override public void onCreate(Bundle args) { arguments = args; start(); }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            // Instrumentation starts before Application.onCreate has finished initializing caches.
            waitForIdleSync();
            if (android.os.Build.VERSION.SDK_INT < 26) throw new IllegalStateException("Device tests require API 26+");
            String ssh = arguments.getString("sshAlias", "codex-profile-ssh");
            String proot = arguments.getString("prootName", "codex-profile-ubuntu");
            for (int round = 0; round < 2; round++) {
                checkBackend(ProfileType.LOCAL, "local", TermuxConstants.TERMUX_HOME_DIR_PATH);
                checkBackend(ProfileType.SSH, ssh, "/root");
                checkBackend(ProfileType.PROOT, proot, "/root");
            }
            result.putString("stream", "\nPROFILE_DEVICE_TESTS_PASSED=" + passed + "\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable error) {
            result.putString("stream", "\nPROFILE_DEVICE_TESTS_FAILED after " + passed + ": " + android.util.Log.getStackTraceString(error) + "\n");
            finish(Activity.RESULT_CANCELED, result);
        }
    }
    private void checkBackend(ProfileType type, String base, String home) throws Exception {
        Profile profile = new Profile("Device " + type, type);
        profile.setBaseId(base);
        profile.setWorkingDirectory("~");
        profile.getEnvironmentVariables().put("HOME", "/must-not-override");
        profile.getEnvironmentVariables().put("PROFILE_LITERAL", "literal ' quote $(exit 99)");
        profile.setStartupCommand("test \"$PWD\" = " + ProfileSessionHelper.quote(home)
            + " && test \"$HOME\" = " + ProfileSessionHelper.quote(home)
            + " && test \"$PROFILE_LITERAL\" = " + ProfileSessionHelper.quote("literal ' quote $(exit 99)")
            + " && printf 'PROFILE_OK\\n' && exit 23");
        run(profile, 23, "PROFILE_OK");
        Profile second = new Profile(profile);
        second.setId(java.util.UUID.randomUUID().toString());
        second.getEnvironmentVariables().put("PROFILE_LITERAL", "second");
        second.setStartupCommand("test \"$PROFILE_LITERAL\" = second && printf 'SECOND_OK\\n'");
        run(second, 0, "SECOND_OK");
        profile.setWorkingDirectory("/codex-profile-does-not-exist-" + java.util.UUID.randomUUID());
        profile.setStartupCommand("printf 'SHOULD_NOT_RUN\\n'");
        run(profile, -1, null);
    }
    private void run(Profile profile, int expected, String marker) throws Exception {
        ExecutionCommand command = ProfileSessionHelper.buildExecutionCommand(profile);
        List<String> argv = new ArrayList<>();
        argv.add(command.executable); argv.addAll(Arrays.asList(command.arguments));
        File log = File.createTempFile("profile-device-", ".log", getTargetContext().getCacheDir());
        ProcessBuilder builder = new ProcessBuilder(argv).redirectErrorStream(true).redirectOutput(log);
        builder.directory(TermuxConstants.TERMUX_HOME_DIR);
        builder.environment().clear();
        builder.environment().putAll(new TermuxShellEnvironment().getEnvironment(getTargetContext(), false));
        builder.environment().put("SHELL", TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH + "/bash");
        Process process = builder.start();
        try {
            process.getOutputStream().close();
            if (!process.waitFor(45, TimeUnit.SECONDS)) throw new AssertionError(profile.getType() + " timeout");
            String output = new String(Files.readAllBytes(log.toPath()), StandardCharsets.UTF_8);
            int code = process.exitValue();
            if ((expected < 0 ? code == 0 : code != expected) ||
                (marker != null && !output.contains(marker)) || output.contains("SHOULD_NOT_RUN"))
                throw new AssertionError(profile.getType() + " exit=" + code + " output=" + output);
            passed++;
            Bundle status = new Bundle(); status.putString("stream", "PASS " + profile.getType() + " case " + passed + "\n");
            sendStatus(0, status);
        } finally { process.destroy(); log.delete(); }
    }
}
