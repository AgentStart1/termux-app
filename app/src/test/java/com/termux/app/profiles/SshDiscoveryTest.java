package com.termux.app.profiles;
import java.io.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class SshDiscoveryTest {
    @Test public void importsAliasesWithoutCopyingOptionsOrRequiringUser() throws Exception {
        List<BaseConnection> bases=SshConfigParser.readHostAliases(new BufferedReader(new StringReader(
            "Host work alternate\n HostName example.com\n ProxyCommand complicated\nHost * !excluded\nHost work\n")));
        assertEquals(2,bases.size());assertEquals("work",bases.get(0).id);assertEquals("alternate",bases.get(1).id);
    }
}
