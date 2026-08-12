package com.termux.app.profiles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Profile存储管理器
 * 处理Profile的JSON存储、SSH配置文件同步和Proot发行版检测
 */
public class ProfileStorageManager {

    private static final String TAG = "ProfileStorageManager";
    private static final String PROFILES_FILE = "profiles.json";
    private static final String SSH_CONFIG_FILE = ".ssh/config";

    private final Context context;
    private final File profilesDir;
    private final File sshConfigFile;

    public ProfileStorageManager(Context context) {
        this.context = context.getApplicationContext();
        this.profilesDir = new File(this.context.getFilesDir(), "profiles");
        this.sshConfigFile = new File(this.context.getFilesDir().getParentFile()
                .getParentFile(), SSH_CONFIG_FILE);
    }

    /**
     * 获取profiles存储目录
     * @return profiles目录
     */
    public File getProfilesDir() {
        return profilesDir;
    }

    /**
     * 获取SSH配置文件路径
     * @return SSH配置文件
     */
    public File getSshConfigFile() {
        return sshConfigFile;
    }

    /**
     * 从JSON文件加载所有profiles
     * @return Profile列表
     */
    public List<Profile> loadProfiles() {
        List<Profile> profiles = new ArrayList<>();
        File profilesFile = new File(profilesDir, PROFILES_FILE);

        if (!profilesFile.exists()) {
            Log.d(TAG, "Profiles file does not exist, returning empty list");
            return profiles;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(profilesFile);
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }

            JSONObject json = new JSONObject(content.toString());
            JSONArray profilesArray = json.optJSONArray("profiles");

            if (profilesArray != null) {
                for (int i = 0; i < profilesArray.length(); i++) {
                    try {
                        Profile profile = Profile.fromJson(profilesArray.getJSONObject(i));
                        profiles.add(profile);
                    } catch (JSONException e) {
                        Log.w(TAG, "Error parsing profile at index " + i, e);
                    }
                }
            }

        } catch (IOException e) {
            Log.e(TAG, "Error reading profiles file", e);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing profiles JSON", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing profiles file", e);
                }
            }
        }

        return profiles;
    }

    /**
     * 保存所有profiles到JSON文件
     * @param profiles Profile列表
     * @param defaultProfileId 默认Profile ID
     * @return 是否成功
     */
    public boolean saveProfiles(List<Profile> profiles, String defaultProfileId) {
        if (profiles == null) {
            return false;
        }

        // 确保目录存在
        if (!profilesDir.exists()) {
            profilesDir.mkdirs();
        }

        FileWriter writer = null;
        try {
            JSONObject json = new JSONObject();
            json.put("defaultProfileId", defaultProfileId);

            JSONArray profilesArray = new JSONArray();
            for (Profile profile : profiles) {
                profilesArray.put(profile.toJson());
            }
            json.put("profiles", profilesArray);

            File profilesFile = new File(profilesDir, PROFILES_FILE);
            writer = new FileWriter(profilesFile);
            writer.write(json.toString(2));
            writer.flush();

            return true;

        } catch (JSONException e) {
            Log.e(TAG, "Error creating profiles JSON", e);
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Error writing profiles file", e);
            return false;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing profiles file", e);
                }
            }
        }
    }

    /**
     * 从JSON文件加载默认Profile ID
     * @return 默认Profile ID，如果没有则返回null
     */
    public String loadDefaultProfileId() {
        File profilesFile = new File(profilesDir, PROFILES_FILE);
        if (!profilesFile.exists()) {
            return null;
        }

        FileReader reader = null;
        try {
            reader = new FileReader(profilesFile);
            StringBuilder content = new StringBuilder();
            char[] buffer = new char[1024];
            int read;
            while ((read = reader.read(buffer)) != -1) {
                content.append(buffer, 0, read);
            }

            JSONObject json = new JSONObject(content.toString());
            return json.optString("defaultProfileId", null);

        } catch (IOException e) {
            Log.e(TAG, "Error reading profiles file", e);
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing profiles JSON", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing profiles file", e);
                }
            }
        }

        return null;
    }

    /**
     * 自动检测已安装的Proot发行版
     * @return 检测到的Proot Profile列表
     */
    public List<Profile> detectProotDistros() {
        List<Profile> prootProfiles = new ArrayList<>();

        try {
            // 执行 proot-distro list 命令
            Process process = Runtime.getRuntime().exec(new String[]{"proot-distro", "list"});
            BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            boolean headerSkipped = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 跳过标题行和空行
                if (line.isEmpty() || line.startsWith("Supported") || line.startsWith("===") || line.startsWith("---")) {
                    headerSkipped = true;
                    continue;
                }

                // 解发行版名称（通常是第一列）
                if (headerSkipped && !line.isEmpty()) {
                    String[] parts = line.split("\\s+");
                    if (parts.length > 0) {
                        String distroName = parts[0].toLowerCase();
                        // 创建Proot Profile
                        Profile prootProfile = createProotProfile(distroName);
                        prootProfiles.add(prootProfile);
                    }
                }
            }

            reader.close();
            process.waitFor();

        } catch (IOException e) {
            Log.d(TAG, "proot-distro not available or not installed", e);
        } catch (InterruptedException e) {
            Log.d(TAG, "proot-distro command interrupted", e);
        }

        return prootProfiles;
    }

    /**
     * 创建Proot Profile
     * @param distroName 发行版名称
     * @return Proot Profile
     */
    private Profile createProotProfile(String distroName) {
        Profile profile = new Profile();
        profile.setName("Proot - " + distroName);
        profile.setType(ProfileType.PROOT);
        profile.setProotDistro(distroName);
        profile.setWorkingDirectory("~");
        return profile;
    }

    /**
     * 从~/.ssh/config导入SSH profiles
     * @return 导入的SSH Profile列表
     */
    public List<Profile> importSshProfiles() {
        return SshConfigParser.parseSshConfig(sshConfigFile);
    }

    /**
     * 将SSH profiles导出到~/.ssh/config
     * @param sshProfiles SSH Profile列表
     * @param preserveExisting 是否保留现有配置
     * @return 是否成功
     */
    public boolean exportSshProfiles(List<Profile> sshProfiles, boolean preserveExisting) {
        return SshConfigParser.writeSshConfig(sshConfigFile, sshProfiles, preserveExisting);
    }

    /**
     * 获取所有profiles（包括自动检测的SSH和Proot）
     * @return 所有Profile列表
     */
    public List<Profile> loadAllProfiles() {
        List<Profile> allProfiles = new ArrayList<>();

        // 加载JSON中的profiles
        List<Profile> jsonProfiles = loadProfiles();
        allProfiles.addAll(jsonProfiles);

        // 从SSH配置导入profiles
        List<Profile> sshProfiles = importSshProfiles();
        for (Profile sshProfile : sshProfiles) {
            // 检查是否已存在（通过host名）
            boolean exists = false;
            for (Profile existing : allProfiles) {
                if (existing.getType() == ProfileType.SSH &&
                    existing.getSshHost() != null &&
                    existing.getSshHost().equals(sshProfile.getSshHost())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                allProfiles.add(sshProfile);
            }
        }

        // 检测已安装的Proot发行版
        List<Profile> prootProfiles = detectProotDistros();
        for (Profile prootProfile : prootProfiles) {
            // 检查是否已存在（通过发行版名称）
            boolean exists = false;
            for (Profile existing : allProfiles) {
                if (existing.getType() == ProfileType.PROOT &&
                    existing.getProotDistro() != null &&
                    existing.getProotDistro().equals(prootProfile.getProotDistro())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                allProfiles.add(prootProfile);
            }
        }

        return allProfiles;
    }

    /**
     * 删除Profile
     * @param profileId Profile ID
     * @return 是否成功
     */
    public boolean deleteProfile(String profileId) {
        List<Profile> profiles = loadProfiles();
        boolean removed = false;

        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getId().equals(profileId)) {
                profiles.remove(i);
                removed = true;
                break;
            }
        }

        if (removed) {
            return saveProfiles(profiles, loadDefaultProfileId());
        }

        return false;
    }

    /**
     * 创建默认的Local Profile
     * @return 默认Local Profile
     */
    public static Profile createDefaultLocalProfile() {
        Profile defaultProfile = new Profile("Local Shell", ProfileType.LOCAL);
        defaultProfile.setWorkingDirectory("~");
        return defaultProfile;
    }
}
