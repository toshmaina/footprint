package com.skyworld.util.memory;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sky-core (ke.co.skyworld.internship.skycore.util.memory)
 * Created by: oloo
 * On: 8/12/26. 5:38 PM
 * Description:
 **/

public class InMemoryCache<V> {

    private final ConcurrentHashMap<String, Entry<V>> store = new ConcurrentHashMap<>();

    public void put(String key, V value, long ttlMs) {
        long expiresAt = Instant.now().toEpochMilli() + ttlMs;
        store.put(key, new Entry<>(value, expiresAt));
        evict();
    }

    public V get(String key) {
        Entry<V> entry = store.get(key);
        if (entry == null) return null;
        if (entry.isExpired()) {
            store.remove(key);
            return null;
        }
        return entry.value;
    }

    public boolean has(String key) {
        return get(key) != null;
    }

    public void remove(String key) {
        store.remove(key);
    }

    private void evict() {
        long now = Instant.now().toEpochMilli();
        store.entrySet().removeIf(e -> e.getValue().expiresAt < now);
    }

    private record Entry<V>(V value, long expiresAt) {
        boolean isExpired() {
            return Instant.now().toEpochMilli() > expiresAt;
        }
    }
}
