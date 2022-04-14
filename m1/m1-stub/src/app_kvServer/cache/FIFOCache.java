package app_kvServer.cache;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.List;
import java.lang.Exception;

import app_kvServer.KeyValue;


public class FIFOCache extends Cache {
    private List<KeyValue> queue;
    private int maxCacheSize;
    private Set keySet;
    public Disk disk;

    public FIFOCache( int cacheSize){
        super(cacheSize);
        this.maxCacheSize = cacheSize;

        this.queue = Collections.synchronizedList(new ArrayList<KeyValue>());
        this.disk = new Disk();
    }

    public String getValue(String key) throws Exception{
        String value;
        if (super._inCache(key)){
            value = super._getValue(key);
        } else {
            value = this.disk.getFromDisk(key);
        }
        return value;
    }

    public boolean putKV(KeyValue KV) {
        try {
            String key = KV.getKey();
            String value = KV.getValue();
            if (this.maxCacheSize == super._readCacheSize()){
                // evict first from cache
                KeyValue kvToEvict = this.queue.remove(0);
                boolean hasEvicted = super._evict(kvToEvict);
            }
            // add to cache
            boolean hasAdded = super._putKeyValue(key, value);
            if (hasAdded == false){
                throw new Exception("Could not Add {"+ key + ":" + value + "} to the cache");
            }
            this.queue.add(KV);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean inCache(String key){
        return super._inCache(key);
    }

    public String onDisk(String key) throws Exception{
        return this.disk.getFromDisk(key);
    }

    public Boolean clearCache() {
        return super._clearCache();
    }


    
}
