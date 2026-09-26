package com.termux.app.profiles;

import android.content.Context;
import android.util.AtomicFile;
import com.termux.shared.termux.TermuxConstants;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Only presets-v2.json is writable. Existing profiles.json and SSH config are untouched. */
public final class ProfileStorageManager implements ProfileStore {
    private final File file;
    ProfileStorageManager(Context context) {
        file = new File(context.getFilesDir(), "profiles/presets-v2.json");
    }
    private JSONObject read() {
        if (!file.exists()) return new JSONObject();
        try {
            return new JSONObject(new String(new AtomicFile(file).readFully(), StandardCharsets.UTF_8));
        } catch (Exception error) { throw new IllegalStateException("Cannot read profiles", error); }
    }
    public List<Profile> loadAllProfiles() {
        List<Profile> profiles = new ArrayList<>();
        JSONArray list = read().optJSONArray("profiles");
        try {
            if (list != null) for (int i = 0; i < list.length(); i++) profiles.add(Profile.fromJson(list.getJSONObject(i)));
        } catch (JSONException error) { throw new IllegalStateException("Invalid profiles", error); }
        return profiles;
    }
    public String loadDefaultProfileId() { return read().optString("defaultProfileId", null); }
    public boolean saveProfiles(List<Profile> profiles, String defaultId) {
        AtomicFile target = new AtomicFile(file);
        FileOutputStream output = null;
        try {
            JSONObject json = new JSONObject();
            JSONArray list = new JSONArray();
            for (Profile profile : profiles) list.put(profile.toJson());
            json.put("profiles", list); json.put("defaultProfileId", defaultId);
            output = target.startWrite();
            output.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
            target.finishWrite(output);
            return true;
        } catch (Exception error) {
            if (output != null) target.failWrite(output);
            return false;
        }
    }
    public List<BaseConnection> discoverConnections() {
        List<BaseConnection> result = new ArrayList<>();
        result.add(new BaseConnection(ProfileType.LOCAL, "local"));
        try {
            result.addAll(SshConfigParser.parseSshConfig(new File(TermuxConstants.TERMUX_HOME_DIR, ".ssh/config")));
        } catch (IOException error) { throw new IllegalStateException("Cannot read SSH config", error); }
        File runtime = new File(TermuxConstants.TERMUX_PREFIX_DIR_PATH, "var/lib/proot-distro");
        Set<String> distros = new TreeSet<>();
        collectInstalled(new File(runtime, "installed-rootfs"), false, distros);
        collectInstalled(new File(runtime, "containers"), true, distros);
        if (new File(TermuxConstants.TERMUX_BIN_PREFIX_DIR_PATH, "proot-distro").canExecute()) {
            for (String distro : distros) result.add(new BaseConnection(ProfileType.PROOT, distro));
        }
        return result;
    }
    private static void collectInstalled(File directory, boolean nested, Set<String> result) {
        File[] entries = directory.listFiles();
        if (entries == null) return;
        for (File entry : entries) {
            File root = nested ? new File(entry, "rootfs") : entry;
            if (root.isDirectory() && Profile.isConcreteSshAlias(entry.getName())) result.add(entry.getName());
        }
    }
}
