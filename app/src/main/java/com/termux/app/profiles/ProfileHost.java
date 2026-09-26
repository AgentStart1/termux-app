package com.termux.app.profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/** All repository access, including construction and disk IO, is confined to serialExecutor. */
final class ProfileHost {
    private final Executor serialExecutor;
    private final StoreFactory storeFactory;
    private ProfileRepository repository;

    ProfileHost(ProfileStore store, Executor serialExecutor) {
        this(() -> store, serialExecutor);
    }

    interface StoreFactory { ProfileStore create(); }

    ProfileHost(StoreFactory storeFactory, Executor serialExecutor) {
        this.storeFactory = storeFactory;
        this.serialExecutor = serialExecutor;
    }

    interface Action<T> { T apply(ProfileRepository repository); }
    interface Reply<T> { void accept(Result<T> result); }

    <T> void submit(Action<T> action, Reply<T> result) {
        serialExecutor.execute(() -> {
            Result<T> response;
            try {
                if (repository == null) repository = new ProfileRepository(storeFactory.create());
                response = new Result<>(action.apply(repository), null);
            } catch (Exception error) {
                response = new Result<>(null, error);
            }
            result.accept(response);
        });
    }

    static final class Result<T> {
        final T value;
        final Exception error;
        Result(T value, Exception error) { this.value = value; this.error = error; }
    }

    static final class Snapshot {
        private final List<Profile> profiles;
        final String defaultId;
        Snapshot(ProfileRepository repository) {
            repository.refresh();
            Profile selected = repository.getDefaultProfile();
            defaultId = selected == null ? null : selected.getId();
            profiles = copy(repository.getProfiles());
        }
        List<Profile> profiles() { return copy(profiles); }
        private static List<Profile> copy(List<Profile> source) {
            List<Profile> result = new ArrayList<>();
            for (Profile profile : source) result.add(new Profile(profile));
            return Collections.unmodifiableList(result);
        }
    }
}
