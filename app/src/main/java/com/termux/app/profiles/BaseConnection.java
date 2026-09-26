package com.termux.app.profiles;

/** Discovered connection, never persisted or edited as a Profile. */
public class BaseConnection {
    public final ProfileType type;
    public final String id;
    public BaseConnection(ProfileType type, String id) { this.type = type; this.id = id; }
    public boolean matches(Profile profile) { return type == profile.getType() && id.equals(profile.getBaseId()); }
    @Override public String toString() { return type.getDisplayName() + " - " + id; }
}
