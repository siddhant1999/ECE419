package app_kvServer.cache;

import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.lang.Exception;

import java.util.Iterator;

import app_kvServer.KeyValue;


public class LFUCache extends Cache {
    
    private Map<String, Integer> lfuHashMap;
    private int maxCacheSize;
    public Disk disk;
    private String lastAdded;

    public LFUCache(int cacheSize){
        super(cacheSize);
        this.lfuHashMap = Collections.synchronizedMap(new HashMap<String, Integer>(this.maxCacheSize));
        this.maxCacheSize = cacheSize;
        this.disk = new Disk();
        this.lastAdded = null;
    }

    public String getValue(String key) throws Exception{

        if (super._inCache(key)){
            updateKey(key);
            String value = super._getValue(key);
            // System.out.println("Getting " + key + " from cache");
            if (this.maxCacheSize == super._readCacheSize() ){
                KeyValue kvToEvict = removeKV();
                boolean hasEvited = super._evict(kvToEvict);
            }
            return value;
        } else {
            String onDisk = this.disk.getFromDisk(key);
            // System.out.println("Getting " + key + " from disk");
            if (!onDisk.equals("null")){
                updateKey(key);
                if (this.maxCacheSize == super._readCacheSize() ){
                    KeyValue kvToEvict = removeKV();
                    boolean hasEvited = super._evict(kvToEvict);
                }
                // boolean hasAdded = super._putKeyValue(key, super._getValue(key));
                // updateKey(key);
            }
            return onDisk;
        }

    }

    public boolean putKV(KeyValue KV){
        try {
            String key = KV.getKey();
            String value = KV.getValue();
            // System.out.println("---+++" + key + ";" + value );
            if ( this.maxCacheSize == super._readCacheSize() ){
                KeyValue kvToEvict = removeKV();
                // System.out.println("after remove kv");
                boolean hasEvited = super._evict(kvToEvict);
            }
            boolean hasAdded = super._putKeyValue(key, value);
            // System.out.println("Added " + key + " = " + Boolean.toString(hasAdded));
            if (hasAdded == false){
                // System.out.println(" HAHAHAHAHAHAHA ");
                throw new Exception("Could not Add {"+ key + ":" + value + "} to the cache");
            }
            this.lfuHashMap.put(key, 1);
            return true;
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    private synchronized void updateKey(String key) throws Exception{
        if (this.lfuHashMap.containsKey(key)){
            Integer freq = this.lfuHashMap.get(key);
            // System.out.println("freq " + freq);
            this.lfuHashMap.put(key, freq + 1);
        } else {
            String value = this.disk.getFromDisk(key);
            boolean hasAdded = super._putKeyValue(key, value );
            this.lastAdded = key;
            // System.out.println("Adding " + key + " to cache = " + Boolean.toString(hasAdded));
            // super._printCache();
            this.lfuHashMap.put(key, 1);
        }

    }

	private synchronized KeyValue removeKV() {
		Map.Entry<String, Integer> lfu = null;
		for (Map.Entry<String, Integer> e : this.lfuHashMap.entrySet()) {
			if (lfu == null || Integer.compare(e.getValue(), lfu.getValue()) < 0 ) { //e.getValue() < lfu.getValue()
				// if (!lfu.getKey().equals(this.lastAdded)){
                    lfu = e;
                    System.out.println("checking keys " + lfu.getKey());
                // }
			}
		}
		String key = lfu.getKey();
		KeyValue kv = new KeyValue(key, super._getValue(key));
        System.out.println("Removing " + key);
		this.lfuHashMap.remove(key);
		return kv;
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

    public synchronized void printLFU(){
        Set<Map.Entry<String, Integer>> entrySet = this.lfuHashMap.entrySet();
        Iterator<Map.Entry<String, Integer> > itr = entrySet.iterator();
        System.out.println("------------------ START LFU ---------------------");
        while (itr.hasNext()){
            Map.Entry<String, Integer> entry = itr.next();
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println("<PRINT LFU> " + key + " : " + Integer.toString(value));
        }
        System.out.println("------------------ END LFU ---------------------");
    }
}
