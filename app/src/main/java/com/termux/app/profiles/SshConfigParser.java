package com.termux.app.profiles;

import java.io.*;
import java.util.*;
import com.termux.shared.shell.ArgumentTokenizer;

/** Read-only discovery. OpenSSH, not this app, evaluates connection options. */
public final class SshConfigParser {
    public static List<BaseConnection> parseSshConfig(File file) throws IOException {
        if (!file.exists()) return new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return readHostAliases(reader);
        }
    }
    static List<BaseConnection> readHostAliases(BufferedReader reader) throws IOException {
        Map<String, BaseConnection> aliases = new LinkedHashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String[] directive = line.trim().split("[\\s=]+", 2);
            if (directive.length != 2 || !directive[0].equalsIgnoreCase("Host")) continue;
            for (String alias : ArgumentTokenizer.tokenizeStrict(directive[1])) {
                if (alias.startsWith("#")) break;
                if (Profile.isConcreteSshAlias(alias))
                    aliases.put(alias, new BaseConnection(ProfileType.SSH, alias));
            }
        }
        return new ArrayList<>(aliases.values());
    }
}
