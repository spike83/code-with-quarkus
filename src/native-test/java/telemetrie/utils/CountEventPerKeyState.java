package telemetrie.utils;

import java.util.concurrent.ConcurrentHashMap;

public class CountEventPerKeyState {

    private final ConcurrentHashMap<String, ThrottlingObject> cache;
    private static CountEventPerKeyState globalCache = null;

    private CountEventPerKeyState() {
        cache = new ConcurrentHashMap<>();
    }

    public static CountEventPerKeyState getInstance() {
        if (globalCache == null){
            globalCache = new CountEventPerKeyState();
        }
        return globalCache;
    }

    public void store(String key, ThrottlingObject value) {
        cache.put(key, value);
    }

    public ThrottlingObject retrieve(String key) {
        if(key == null){
            return null;
        }
        return cache.get(key);
    }

    public ThrottlingObject remove(String key) {
        return cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }
}
