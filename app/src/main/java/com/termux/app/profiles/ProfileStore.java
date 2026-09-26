package com.termux.app.profiles;
import java.util.List;
interface ProfileStore {
    List<Profile> loadAllProfiles();
    String loadDefaultProfileId();
    boolean saveProfiles(List<Profile> profiles, String defaultProfileId);
    List<BaseConnection> discoverConnections();
}
