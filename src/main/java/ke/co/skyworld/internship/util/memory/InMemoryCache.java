package ke.co.skyworld.internship.util.memory;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * sky-core (ke.co.skyworld.internship.skycore.util.memory)
 * Created by: oloo
 * On: 8/12/26. 5:38 PM
 * Description:
 **/

public final class InMemoryCache {

    private static final ConcurrentHashMap<String, Entry> store =
            new ConcurrentHashMap<>();

    private InMemoryCache() {
    }

    public static <V> void put(String key, V value, long ttlMs) {
        if (ttlMs <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }

        long expiresAt = Math.addExact(
                Instant.now().toEpochMilli(),
                ttlMs
        );

        store.put(key, new Entry(value, expiresAt));
        evict();
    }

    @SuppressWarnings("unchecked")
    public static <V> V get(String key) {
        Entry entry = store.get(key);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            store.remove(key, entry);
            return null;
        }

        return (V) entry.value();
    }

    public static boolean has(String key) {
        return get(key) != null;
    }

    public static boolean remove(String key) {
        return store.remove(key) != null;
    }

    private static void evict() {
        long now = Instant.now().toEpochMilli();

        store.entrySet().removeIf(
                entry -> entry.getValue().expiresAt() <= now
        );
    }

    private record Entry(Object value, long expiresAt) {

        boolean isExpired() {
            return Instant.now().toEpochMilli() >= expiresAt;
        }
    }
}
