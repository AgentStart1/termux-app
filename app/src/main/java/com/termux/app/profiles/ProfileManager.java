package com.termux.app.profiles;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

/**
 * Profile管理器单例
 * 提供Profile的CRUD操作和管理功能
 */
public class ProfileManager {

    private static final String TAG = "ProfileManager";

    private static ProfileManager instance;
    private final ProfileStorageManager storageManager;
    private List<Profile> profiles;
    private String defaultProfileId;

    private ProfileManager(Context context) {
        this.storageManager = new ProfileStorageManager(context);
        loadProfiles();
    }

    /**
     * 获取ProfileManager单例实例
     * @param context Context
     * @return ProfileManager实例
     */
    public static synchronized ProfileManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProfileManager(context);
        }
        return instance;
    }

    /**
     * 加载所有profiles
     */
    private void loadProfiles() {
        this.profiles = storageManager.loadAllProfiles();
        this.defaultProfileId = storageManager.loadDefaultProfileId();

        // 如果没有profiles，创建默认的Local profile
        if (profiles.isEmpty()) {
            Profile defaultProfile = ProfileStorageManager.createDefaultLocalProfile();
            profiles.add(defaultProfile);
            this.defaultProfileId = defaultProfile.getId();
            saveProfiles();
        }

        Log.d(TAG, "Loaded " + profiles.size() + " profiles");
    }

    /**
     * 保存所有profiles
     * @return 是否成功
     */
    private boolean saveProfiles() {
        return storageManager.saveProfiles(profiles, defaultProfileId);
    }

    /**
     * 获取所有profiles
     * @return Profile列表
     */
    public List<Profile> getProfiles() {
        return new ArrayList<>(profiles);
    }

    /**
     * 根据ID获取profile
     * @param profileId Profile ID
     * @return Profile，如果不存在则返回null
     */
    public Profile getProfile(String profileId) {
        if (profileId == null) {
            return null;
        }

        for (Profile profile : profiles) {
            if (profile.getId().equals(profileId)) {
                return profile;
            }
        }

        return null;
    }

    /**
     * 获取默认profile
     * @return 默认Profile
     */
    public Profile getDefaultProfile() {
        if (defaultProfileId != null) {
            Profile defaultProfile = getProfile(defaultProfileId);
            if (defaultProfile != null) {
                return defaultProfile;
            }
        }

        // 如果默认profile不存在，返回第一个profile
        if (!profiles.isEmpty()) {
            return profiles.get(0);
        }

        // 如果没有任何profile，创建默认的Local profile
        Profile defaultProfile = ProfileStorageManager.createDefaultLocalProfile();
        profiles.add(defaultProfile);
        this.defaultProfileId = defaultProfile.getId();
        saveProfiles();
        return defaultProfile;
    }

    /**
     * 设置默认profile
     * @param profileId Profile ID
     * @return 是否成功
     */
    public boolean setDefaultProfile(String profileId) {
        Profile profile = getProfile(profileId);
        if (profile == null) {
            return false;
        }

        this.defaultProfileId = profileId;
        return saveProfiles();
    }

    /**
     * 创建新profile
     * @param profile 新Profile
     * @return 是否成功
     */
    public boolean createProfile(Profile profile) {
        if (profile == null || !profile.isValid()) {
            return false;
        }

        // 检查是否已存在同名profile
        for (Profile existing : profiles) {
            if (existing.getName().equals(profile.getName())) {
                Log.w(TAG, "Profile with name already exists: " + profile.getName());
                return false;
            }
        }

        profiles.add(profile);

        // 如果是第一个profile，设置为默认
        if (profiles.size() == 1) {
            this.defaultProfileId = profile.getId();
        }

        return saveProfiles();
    }

    /**
     * 更新现有profile
     * @param profile 更新后的Profile
     * @return 是否成功
     */
    public boolean updateProfile(Profile profile) {
        if (profile == null || !profile.isValid()) {
            return false;
        }

        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getId().equals(profile.getId())) {
                profiles.set(i, profile);
                return saveProfiles();
            }
        }

        return false;
    }

    /**
     * 删除profile
     * @param profileId Profile ID
     * @return 是否成功
     */
    public boolean deleteProfile(String profileId) {
        if (profileId == null) {
            return false;
        }

        // 不能删除默认profile
        if (profileId.equals(defaultProfileId)) {
            Log.w(TAG, "Cannot delete default profile");
            return false;
        }

        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getId().equals(profileId)) {
                profiles.remove(i);
                return saveProfiles();
            }
        }

        return false;
    }

    /**
     * 获取Local profiles
     * @return Local Profile列表
     */
    public List<Profile> getLocalProfiles() {
        List<Profile> localProfiles = new ArrayList<>();
        for (Profile profile : profiles) {
            if (profile.getType() == ProfileType.LOCAL) {
                localProfiles.add(profile);
            }
        }
        return localProfiles;
    }

    /**
     * 获取Proot profiles
     * @return Proot Profile列表
     */
    public List<Profile> getProotProfiles() {
        List<Profile> prootProfiles = new ArrayList<>();
        for (Profile profile : profiles) {
            if (profile.getType() == ProfileType.PROOT) {
                prootProfiles.add(profile);
            }
        }
        return prootProfiles;
    }

    /**
     * 获取SSH profiles
     * @return SSH Profile列表
     */
    public List<Profile> getSshProfiles() {
        List<Profile> sshProfiles = new ArrayList<>();
        for (Profile profile : profiles) {
            if (profile.getType() == ProfileType.SSH) {
                sshProfiles.add(profile);
            }
        }
        return sshProfiles;
    }

    /**
     * 从~/.ssh/config导入SSH profiles
     * @return 导入的SSH Profile数量
     */
    public int importSshProfiles() {
        List<Profile> importedProfiles = storageManager.importSshProfiles();
        int imported = 0;

        for (Profile importedProfile : importedProfiles) {
            // 检查是否已存在
            boolean exists = false;
            for (Profile existing : profiles) {
                if (existing.getType() == ProfileType.SSH &&
                    existing.getSshHost() != null &&
                    existing.getSshHost().equals(importedProfile.getSshHost())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                profiles.add(importedProfile);
                imported++;
            }
        }

        if (imported > 0) {
            saveProfiles();
        }

        return imported;
    }

    /**
     * 刷新profiles（重新加载）
     */
    public void refresh() {
        loadProfiles();
    }

    /**
     * 异步检测并添加Proot发行版
     * @param callback 检测完成后的回调，返回新添加的数量
     */
    public void detectAndAddProotDistrosAsync(ProotDetectionCallback callback) {
        storageManager.detectProotDistrosAsync(detectedDistros -> {
            int added = 0;
            for (Profile detected : detectedDistros) {
                boolean exists = false;
                for (Profile existing : profiles) {
                    if (existing.getType() == ProfileType.PROOT &&
                        existing.getProotDistro() != null &&
                        existing.getProotDistro().equals(detected.getProotDistro())) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    profiles.add(detected);
                    added++;
                }
            }
            if (added > 0) {
                saveProfiles();
            }
            if (callback != null) {
                callback.onProotDetected(added);
            }
        });
    }

    /**
     * Proot检测回调接口
     */
    public interface ProotDetectionCallback {
        void onProotDetected(int addedCount);
    }
}
