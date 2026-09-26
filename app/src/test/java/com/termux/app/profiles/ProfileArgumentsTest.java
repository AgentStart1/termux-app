package com.termux.app.profiles;
import org.junit.Test;
import com.termux.shared.shell.command.ExecutionCommand;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.Assert.*;

public class ProfileArgumentsTest {
    @Test public void unchangedBasesUseOnlyTheirReference() {
        Profile ssh=new Profile("SSH",ProfileType.SSH);ssh.setBaseId("work");
        assertArrayEquals(new String[]{"work"},ProfileSessionHelper.buildExecutionCommand(ssh).arguments);
        Profile proot=new Profile("Proot",ProfileType.PROOT);proot.setBaseId("debian");
        assertArrayEquals(new String[]{"login","debian"},ProfileSessionHelper.buildExecutionCommand(proot).arguments);
        assertNull(ProfileSessionHelper.buildExecutionCommand(new Profile("Local",ProfileType.LOCAL)).executable);
    }
    @Test public void remoteDirectoryNeverBecomesLocalProcessDirectory() {
        Profile p=new Profile("SSH",ProfileType.SSH);p.setBaseId("work");p.setWorkingDirectory("/remote/project");
        ExecutionCommand c=ProfileSessionHelper.buildExecutionCommand(p);
        assertNull(c.workingDirectory);assertEquals("work",c.arguments[0]);
        assertTrue(c.arguments[1].contains("/remote/project"));
    }
    @Test public void invalidReferenceAndEnvironmentNamesAreRejected() {
        Profile p=new Profile("bad",ProfileType.SSH);p.setBaseId("-oProxyCommand=bad");
        assertThrows(IllegalArgumentException.class,()->ProfileSessionHelper.buildExecutionCommand(p));
        p.setType(ProfileType.LOCAL);p.setBaseId("local");
        p.getEnvironmentVariables().put("BAD;NAME","value");
        assertThrows(IllegalArgumentException.class,()->ProfileSessionHelper.buildExecutionCommand(p));
    }
    @Test public void extensionsExecuteSafelyAndPreserveExitStatus() throws Exception {
        Profile p=new Profile("test",ProfileType.LOCAL);
        p.getEnvironmentVariables().put("EXISTING","replacement");
        p.getEnvironmentVariables().put("ADDED","literal ' $(echo injected)");
        p.setWorkingDirectory("~");
        p.setStartupCommand("printf '%s\\n' \"$EXISTING\" \"$ADDED\"; exit 23");
        ProcessBuilder builder=shell();
        builder.environment().put("EXISTING","");
        builder.environment().remove("ADDED");
        builder.environment().put("HOME",System.getProperty("java.io.tmpdir").replace('\\','/'));
        builder.environment().put("SHELL","/bin/sh");
        Process process=builder.redirectErrorStream(true).start();
        try (OutputStream input=process.getOutputStream()) {
            input.write(("EXISTING=''; export EXISTING\n" + ProfileSessionHelper.extensionScript(p)).getBytes(StandardCharsets.UTF_8));
        }
        String output=new String(read(process.getInputStream()),StandardCharsets.UTF_8);
        assertEquals(output,23,process.waitFor());assertEquals("\nliteral ' $(echo injected)\n",output.replace("\r",""));
    }
    @Test public void missingDirectoryPreventsCommand() throws Exception {
        Profile p=new Profile("test",ProfileType.LOCAL);
        p.setWorkingDirectory("/nonexistent-profile-dir-"+UUID.randomUUID());
        p.setStartupCommand("echo SHOULD_NOT_RUN");
        Process process=shell().redirectErrorStream(true).start();
        try (OutputStream input=process.getOutputStream()) {
            input.write(ProfileSessionHelper.extensionScript(p).getBytes(StandardCharsets.UTF_8));
        }
        String output=new String(read(process.getInputStream()),StandardCharsets.UTF_8);
        assertNotEquals(0,process.waitFor());assertFalse(output.contains("SHOULD_NOT_RUN"));
    }
    private static ProcessBuilder shell() {
        String executable=System.getProperty("os.name").startsWith("Windows") ?
            "C:/Program Files/Git/bin/bash.exe" : "/bin/sh";
        return new ProcessBuilder(executable,"-s");
    }
    private static byte[] read(InputStream stream) throws IOException {
        ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[1024];int count;
        while((count=stream.read(buffer))!=-1)out.write(buffer,0,count);return out.toByteArray();
    }
}
