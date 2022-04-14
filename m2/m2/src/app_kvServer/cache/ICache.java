package app_kvServer.cache;

public interface ICache {
    String get(String key);
    Boolean inCache(String key);
    void put(String key, String val);
    void clearCache();
}
