package com.termux.app.profiles;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Android lifecycle adapter. All profile IO and mutations run on the application queue. */
public final class ProfileManager {
    private static final ExecutorService PROFILE_EXECUTOR = Executors.newSingleThreadExecutor();
    private static ProfileManager instance;
    private final ProfileHost host;
    private final Context context;

    private ProfileManager(Context context) {
        this.context = context.getApplicationContext();
        host = new ProfileHost(() -> new ProfileStorageManager(this.context), PROFILE_EXECUTOR);
    }

    public static synchronized ProfileManager getInstance(Context context) {
        if (instance == null) instance = new ProfileManager(context);
        return instance;
    }

    public interface Callback<T> { void accept(T value); }

    private <T> void request(LifecycleOwner owner, ProfileHost.Action<T> action, Callback<T> callback) {
        request(owner, action, callback, () -> {});
    }

    private <T> void request(LifecycleOwner owner, ProfileHost.Action<T> action,
                             Callback<T> callback, Runnable onError) {
        MutableLiveData<ProfileHost.Result<T>> result = new MutableLiveData<>();
        result.observe(owner, response -> {
            result.removeObservers(owner);
            if (response.error != null) {
                Log.e("ProfileManager", "Profile operation failed", response.error);
                Toast.makeText(context, response.error.getMessage(), Toast.LENGTH_LONG).show();
                onError.run();
            } else {
                callback.accept(response.value);
            }
        });
        host.submit(action, result::postValue);
    }

    public void initialize() {
        host.submit(repository -> null, result -> {
            if (result.error != null) Log.e("ProfileManager", "Profile loading failed", result.error);
        });
    }

    void snapshot(LifecycleOwner owner, Callback<ProfileHost.Snapshot> callback) {
        request(owner, ProfileHost.Snapshot::new, callback);
    }

    public void getProfile(LifecycleOwner owner, String id, Callback<Profile> callback) {
        request(owner, repository -> {
            Profile profile = repository.getProfile(id);
            return profile == null ? null : new Profile(profile);
        }, callback);
    }

    public void getDefaultProfile(LifecycleOwner owner, Callback<Profile> callback) {
        request(owner, repository -> {
            Profile profile = repository.getDefaultProfile();
            return profile == null ? null : new Profile(profile);
        }, callback);
    }

    public void saveProfile(LifecycleOwner owner, Profile profile, boolean editing, Callback<Boolean> callback) {
        Profile copy = new Profile(profile);
        request(owner, repository -> editing ? repository.updateProfile(copy) : repository.createProfile(copy),
            callback, () -> callback.accept(false));
    }

    public void deleteProfile(LifecycleOwner owner, String id, Callback<Boolean> callback) {
        request(owner, repository -> repository.deleteProfile(id), callback);
    }

    public void setDefaultProfile(LifecycleOwner owner, String id, Callback<Boolean> callback) {
        request(owner, repository -> repository.setDefaultProfile(id), callback);
    }

    public void connections(LifecycleOwner owner, Callback<java.util.List<BaseConnection>> callback) {
        request(owner, ProfileRepository::connections, callback);
    }

    public void resolveForLaunch(LifecycleOwner owner, String id, Callback<Profile> callback) {
        request(owner, repository -> {
            repository.refresh();
            Profile profile = repository.getProfile(id);
            if (profile == null || !profile.isAvailable())
                throw new IllegalStateException("Base connection unavailable; edit the profile to rebind");
            return new Profile(profile);
        }, callback);
    }

    public void refresh(LifecycleOwner owner, Callback<Boolean> callback) {
        request(owner, repository -> { repository.refresh(); return true; }, callback);
    }
}
