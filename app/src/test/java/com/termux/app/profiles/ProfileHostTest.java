package com.termux.app.profiles;
import org.junit.Test;
import java.util.*;
import java.util.concurrent.Executor;
import static org.junit.Assert.*;

public class ProfileHostTest {
    static class Queue implements Executor {
        final ArrayDeque<Runnable> tasks = new ArrayDeque<>();
        boolean running;
        public void execute(Runnable task) { tasks.add(task); }
        void drain() { running=true; try { while (!tasks.isEmpty()) tasks.remove().run(); } finally { running=false; } }
    }
    static class Store implements ProfileStore {
        final Queue queue;
        List<BaseConnection> bases = new ArrayList<>(Arrays.asList(new BaseConnection(ProfileType.SSH,"work")));
        List<Profile> saved = new ArrayList<>();
        boolean fail;
        Store(Queue queue) { this.queue=queue; }
        void check() { assertTrue(queue.running); }
        public List<Profile> loadAllProfiles() { check(); return new ArrayList<>(); }
        public String loadDefaultProfileId() { check(); return null; }
        public boolean saveProfiles(List<Profile> profiles,String id) { check(); if(fail)return false; saved=new ArrayList<>(profiles); return true; }
        public List<BaseConnection> discoverConnections() { check(); return bases; }
    }
    static Profile preset(String name) { Profile p=new Profile(name,ProfileType.SSH);p.setBaseId("work");return p; }
    @Test public void multiplePresetsReferenceSameBaseAndDiscoveryDoesNotCreateProfiles() {
        Queue queue=new Queue(); Store store=new Store(queue); ProfileHost host=new ProfileHost(store,queue);
        Profile a=preset("one"), b=preset("two");
        host.submit(repository -> {
            assertTrue(repository.getProfiles().isEmpty());
            assertTrue(repository.createProfile(a));assertTrue(repository.createProfile(b));
            repository.refresh();assertEquals(2,repository.getProfiles().size());
            return new ProfileHost.Snapshot(repository);
        }, result -> { assertNull(result.error); assertEquals(2,result.value.profiles().size()); });
        assertTrue(store.saved.isEmpty());queue.drain();
    }
    @Test public void missingBaseIsRetainedAndCanBeRebound() {
        Queue queue=new Queue(); Store store=new Store(queue);ProfileHost host=new ProfileHost(store,queue);
        host.submit(repository -> {
            Profile p=preset("one");repository.createProfile(p);store.bases.clear();repository.refresh();
            assertFalse(repository.getProfile(p.getId()).isAvailable());
            assertEquals(1,repository.getProfiles().size());
            p.setType(ProfileType.LOCAL);p.setBaseId("local");
            store.bases.add(new BaseConnection(ProfileType.LOCAL,"local"));
            assertTrue(repository.updateProfile(p));repository.refresh();
            assertTrue(repository.getProfile(p.getId()).isAvailable());return true;
        }, result -> assertNull(result.error));queue.drain();
    }
    @Test public void failedWriteDoesNotMutateStateAndSnapshotsAreDetached() {
        Queue queue=new Queue();Store store=new Store(queue);ProfileHost host=new ProfileHost(store,queue);
        host.submit(repository -> {
            Profile p=preset("one");repository.createProfile(p);
            ProfileHost.Snapshot snapshot=new ProfileHost.Snapshot(repository);
            snapshot.profiles().get(0).setName("not stored");
            assertEquals("one",snapshot.profiles().get(0).getName());
            store.fail=true;assertFalse(repository.deleteProfile(p.getId()));
            assertNotNull(repository.getProfile(p.getId()));return true;
        }, result -> assertNull(result.error));queue.drain();
    }
    @Test public void failureDoesNotStopFollowingRequest() {
        Queue queue=new Queue();Store store=new Store(queue);ProfileHost host=new ProfileHost(store,queue);
        host.submit(repository -> { throw new IllegalStateException("test"); }, result -> assertNotNull(result.error));
        host.submit(repository -> repository.getProfiles().size(), result -> assertEquals(Integer.valueOf(0),result.value));
        queue.drain();
    }
}
