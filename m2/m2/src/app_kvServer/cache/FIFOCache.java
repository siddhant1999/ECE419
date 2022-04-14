package app_kvServer.cache;

import java.security.Key;
import java.util.*;
import java.lang.Exception;
import java.util.concurrent.ConcurrentHashMap;

import app_kvServer.KeyValue;


public class FIFOCache implements ICache{
    private int cacheSize;
    private Map<String, KeyValue> hashMap;
    private Queue<KeyValue> queue;

    public FIFOCache(int cacheSize){
        this.cacheSize = cacheSize;
        this.hashMap = new ConcurrentHashMap<String, KeyValue>(this.cacheSize);
        this.queue = new LinkedList<>();
    }

    public String get(String key){
        if (inCache(key)) {
            return this.hashMap.get(key).getValue();
        }
        return null; //value not in cache, will not hit, since taken care of by CacheImplement
    }

    public Boolean inCache(String key) {
        return this.hashMap.containsKey(key);
    }

    /**
     *
     * @param key
     * @param value
     * @return True -> Works, False -> Not Works
     */
    public void put(String key, String value) {
        if (value.equals("null")) {
            if (inCache(key)) {
                queue.remove(this.hashMap.remove(key));
            }
            return;
        }
        if (this.hashMap.size() == cacheSize){
            KeyValue popped = queue.remove();
            //remove value from hashMap
            hashMap.remove(popped.getKey());
        }
        hashMap.put(key,new KeyValue(key, value));
        queue.add(hashMap.get(key));
    }

    public void clearCache(){
        this.hashMap.clear();
    }


    
}
