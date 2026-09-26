package com.termux.app.profiles;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
public class ProfilePersistenceTest {
    @Test public void roundTripStoresOnlyReferenceAndExtensions() throws Exception {
        Profile p=new Profile("Development",ProfileType.SSH);
        p.setBaseId("work");p.setWorkingDirectory("~/project");p.setStartupCommand("exit 7");
        p.getEnvironmentVariables().put("PROFILE_MODE","development");
        org.json.JSONObject json=p.toJson();
        assertFalse(json.has("sshHost"));assertFalse(json.has("sshUser"));assertFalse(json.has("sshArgs"));
        Profile restored=Profile.fromJson(json);
        assertEquals(p.getId(),restored.getId());assertEquals("work",restored.getBaseId());
        assertEquals("~/project",restored.getWorkingDirectory());assertEquals("exit 7",restored.getStartupCommand());
        assertEquals("development",restored.getEnvironmentVariables().get("PROFILE_MODE"));
    }
}
