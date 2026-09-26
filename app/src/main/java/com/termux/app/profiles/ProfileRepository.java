package com.termux.app.profiles;
import java.util.*;

/** Confined to the ProfileHost serial executor. Discovery never creates presets. */
final class ProfileRepository {
    private final ProfileStore store;
    private List<Profile> profiles;
    private String defaultId;
    ProfileRepository(ProfileStore store) {
        this.store = store;
        profiles = store.loadAllProfiles();
        defaultId = store.loadDefaultProfileId();
    }
    public List<BaseConnection> connections() { return store.discoverConnections(); }
    public void refresh() {
        List<BaseConnection> bases = connections();
        for (Profile profile : profiles) {
            profile.setAvailable(false);
            for (BaseConnection base : bases) if (base.matches(profile)) profile.setAvailable(true);
        }
    }
    public List<Profile> getProfiles() { return new ArrayList<>(profiles); }
    public Profile getProfile(String id) {
        for (Profile profile : profiles) if (profile.getId().equals(id)) return profile;
        return null;
    }
    public Profile getDefaultProfile() { return getProfile(defaultId); }
    public boolean setDefaultProfile(String id) {
        if (getProfile(id) == null || !store.saveProfiles(profiles, id)) return false;
        defaultId = id; return true;
    }
    public boolean createProfile(Profile profile) {
        if (profile == null || !profile.isValid() || getProfile(profile.getId()) != null) return false;
        List<Profile> next = getProfiles(); next.add(new Profile(profile));
        return commit(next, defaultId == null ? profile.getId() : defaultId);
    }
    public boolean updateProfile(Profile profile) {
        if (profile == null || !profile.isValid()) return false;
        List<Profile> next = getProfiles();
        for (int i = 0; i < next.size(); i++) if (next.get(i).getId().equals(profile.getId())) {
            next.set(i, new Profile(profile)); return commit(next, defaultId);
        }
        return false;
    }
    public boolean deleteProfile(String id) {
        if (getProfile(id) == null) return false;
        List<Profile> next = getProfiles();
        java.util.Iterator<Profile> iterator = next.iterator();
        while (iterator.hasNext()) if (iterator.next().getId().equals(id)) iterator.remove();
        return commit(next, Objects.equals(id, defaultId) ? null : defaultId);
    }
    private boolean commit(List<Profile> next, String nextDefault) {
        if (!store.saveProfiles(next, nextDefault)) return false;
        profiles = next; defaultId = nextDefault; return true;
    }
}
