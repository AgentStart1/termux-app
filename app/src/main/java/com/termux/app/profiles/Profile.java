package com.termux.app.profiles;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

/** A launch preset referencing a read-only base connection. */
public class Profile {
    private String id = UUID.randomUUID().toString();
    private String name = "";
    private ProfileType type = ProfileType.LOCAL;
    private String baseId = "local";
    private String workingDirectory = "";
    private String startupCommand = "";
    private Map<String, String> environmentVariables = new LinkedHashMap<>();
    private boolean available = true;

    public Profile() {}
    public Profile(String name, ProfileType type) {
        this.name = name;
        this.type = type;
        this.baseId = type == ProfileType.LOCAL ? "local" : "";
    }
    public Profile(Profile source) {
        id = source.id; name = source.name; type = source.type; baseId = source.baseId;
        workingDirectory = source.workingDirectory; startupCommand = source.startupCommand;
        environmentVariables = new LinkedHashMap<>(source.environmentVariables);
        available = source.available;
    }
    public String getId() { return id; }
    public void setId(String value) { id = value; }
    public String getName() { return name; }
    public void setName(String value) { name = value; }
    public ProfileType getType() { return type; }
    public void setType(ProfileType value) { type = value; }
    public String getBaseId() { return baseId; }
    public void setBaseId(String value) { baseId = value; }
    public String getWorkingDirectory() { return workingDirectory; }
    public void setWorkingDirectory(String value) { workingDirectory = value; }
    public String getStartupCommand() { return startupCommand; }
    public void setStartupCommand(String value) { startupCommand = value; }
    public Map<String, String> getEnvironmentVariables() { return environmentVariables; }
    public void setEnvironmentVariables(Map<String, String> value) { environmentVariables = new LinkedHashMap<>(value); }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean value) { available = value; }

    static boolean isConcreteSshAlias(String alias) {
        return alias != null && alias.matches("[a-zA-Z0-9_][a-zA-Z0-9_.:-]*");
    }
    public boolean isValid() {
        if (name == null || name.trim().isEmpty() || type == null || baseId == null) return false;
        if (type == ProfileType.LOCAL ? !baseId.equals("local") : !isConcreteSshAlias(baseId)) return false;
        if (workingDirectory == null || startupCommand == null ||
            workingDirectory.indexOf(0) >= 0 || startupCommand.indexOf(0) >= 0) return false;
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            if (!entry.getKey().matches("[A-Za-z_][A-Za-z0-9_]*") ||
                entry.getValue() == null || entry.getValue().indexOf(0) >= 0) return false;
        }
        return true;
    }
    public String getDescription() {
        return type.getDisplayName() + " - " + baseId + (available ? "" : " (unavailable; edit to rebind)");
    }
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id); json.put("name", name); json.put("type", type.getValue());
        json.put("baseId", baseId); json.put("workingDirectory", workingDirectory);
        json.put("startupCommand", startupCommand);
        JSONObject env = new JSONObject();
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) env.put(entry.getKey(), entry.getValue());
        json.put("environmentVariables", env);
        return json;
    }
    public static Profile fromJson(JSONObject json) throws JSONException {
        Profile profile = new Profile(json.getString("name"), ProfileType.fromValue(json.getString("type")));
        profile.id = json.getString("id"); profile.baseId = json.getString("baseId");
        profile.workingDirectory = json.optString("workingDirectory", "");
        profile.startupCommand = json.optString("startupCommand", "");
        JSONObject env = json.optJSONObject("environmentVariables");
        if (env != null) {
            java.util.Iterator<String> keys = env.keys();
            while (keys.hasNext()) { String key = keys.next(); profile.environmentVariables.put(key, env.getString(key)); }
        }
        if (!profile.isValid()) throw new JSONException("Invalid profile");
        return profile;
    }
}
