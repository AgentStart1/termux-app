package com.termux.app.profiles;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Profile数据模型类
 * 支持Local、Proot、SSH三种类型的shell配置
 */
public class Profile {

    private String id;
    private String name;
    private ProfileType type;
    private String shell;
    private String workingDirectory;
    private Map<String, String> environmentVariables;

    // Proot相关字段
    private String prootDistro;
    private String prootArgs;
    private String prootCommand;

    // SSH相关字段
    private String sshHost;
    private int sshPort;
    private String sshUser;
    private String sshKeyFile;
    private String sshArgs;

    /**
     * 默认构造函数
     */
    public Profile() {
        this.id = UUID.randomUUID().toString();
        this.type = ProfileType.LOCAL;
        this.environmentVariables = new HashMap<>();
        this.sshPort = 22;
    }

    /**
     * 创建新的Profile
     * @param name Profile名称
     * @param type Profile类型
     */
    public Profile(String name, ProfileType type) {
        this();
        this.name = name;
        this.type = type;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProfileType getType() {
        return type;
    }

    public void setType(ProfileType type) {
        this.type = type;
    }

    public String getShell() {
        return shell;
    }

    public void setShell(String shell) {
        this.shell = shell;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    // Proot getters and setters

    public String getProotDistro() {
        return prootDistro;
    }

    public void setProotDistro(String prootDistro) {
        this.prootDistro = prootDistro;
    }

    public String getProotArgs() {
        return prootArgs;
    }

    public void setProotArgs(String prootArgs) {
        this.prootArgs = prootArgs;
    }

    public String getProotCommand() {
        return prootCommand;
    }

    public void setProotCommand(String prootCommand) {
        this.prootCommand = prootCommand;
    }

    // SSH getters and setters

    public String getSshHost() {
        return sshHost;
    }

    public void setSshHost(String sshHost) {
        this.sshHost = sshHost;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public String getSshUser() {
        return sshUser;
    }

    public void setSshUser(String sshUser) {
        this.sshUser = sshUser;
    }

    public String getSshKeyFile() {
        return sshKeyFile;
    }

    public void setSshKeyFile(String sshKeyFile) {
        this.sshKeyFile = sshKeyFile;
    }

    public String getSshArgs() {
        return sshArgs;
    }

    public void setSshArgs(String sshArgs) {
        this.sshArgs = sshArgs;
    }

    /**
     * 将Profile转换为JSON对象
     * @return JSONObject
     * @throws JSONException 如果序列化失败
     */
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("type", type.getValue());
        json.put("shell", shell);
        json.put("workingDirectory", workingDirectory);

        // 环境变量
        JSONObject envJson = new JSONObject();
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            envJson.put(entry.getKey(), entry.getValue());
        }
        json.put("environmentVariables", envJson);

        // Proot相关
        json.put("prootDistro", prootDistro);
        json.put("prootArgs", prootArgs);
        json.put("prootCommand", prootCommand);

        // SSH相关
        json.put("sshHost", sshHost);
        json.put("sshPort", sshPort);
        json.put("sshUser", sshUser);
        json.put("sshKeyFile", sshKeyFile);
        json.put("sshArgs", sshArgs);

        return json;
    }

    /**
     * 从JSON对象创建Profile
     * @param json JSONObject
     * @return Profile实例
     * @throws JSONException 如果反序列化失败
     */
    public static Profile fromJson(JSONObject json) throws JSONException {
        Profile profile = new Profile();
        profile.id = json.getString("id");
        profile.name = json.getString("name");
        profile.type = ProfileType.fromValue(json.getString("type"));
        profile.shell = json.optString("shell", null);
        profile.workingDirectory = json.optString("workingDirectory", null);

        // 环境变量
        profile.environmentVariables = new HashMap<>();
        if (json.has("environmentVariables")) {
            JSONObject envJson = json.getJSONObject("environmentVariables");
            for (java.util.Iterator<String> keys = envJson.keys(); keys.hasNext(); ) {
                String key = keys.next();
                profile.environmentVariables.put(key, envJson.getString(key));
            }
        }

        // Proot相关
        profile.prootDistro = json.optString("prootDistro", null);
        profile.prootArgs = json.optString("prootArgs", null);
        profile.prootCommand = json.optString("prootCommand", null);

        // SSH相关
        profile.sshHost = json.optString("sshHost", null);
        profile.sshPort = json.optInt("sshPort", 22);
        profile.sshUser = json.optString("sshUser", null);
        profile.sshKeyFile = json.optString("sshKeyFile", null);
        profile.sshArgs = json.optString("sshArgs", null);

        return profile;
    }

    /**
     * 验证Profile配置是否有效
     * @return 是否有效
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        switch (type) {
            case LOCAL:
                return true;
            case PROOT:
                return prootDistro != null && !prootDistro.trim().isEmpty();
            case SSH:
                return sshHost != null && !sshHost.trim().isEmpty() &&
                       sshUser != null && !sshUser.trim().isEmpty();
            default:
                return false;
        }
    }

    /**
     * 获取Profile的显示描述
     * @return 描述字符串
     */
    public String getDescription() {
        switch (type) {
            case LOCAL:
                return "Local Shell";
            case PROOT:
                return "Proot - " + (prootDistro != null ? prootDistro : "Unknown");
            case SSH:
                return "SSH - " + (sshUser != null ? sshUser : "") +
                       "@" + (sshHost != null ? sshHost : "") +
                       (sshPort != 22 ? ":" + sshPort : "");
            default:
                return "Unknown";
        }
    }

    @Override
    public String toString() {
        return "Profile{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", type=" + type +
               ", description='" + getDescription() + '\'' +
               '}';
    }
}
