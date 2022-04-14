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
//Deprecated
public class LRUCache{
}
//public class LRUCache extends Cache {
//
//    private List<KeyValue> lruQueue;
//    private int maxCacheSize;
//    public Disk disk;
//
//    public LRUCache(int cacheSize){
//        super(cacheSize);
//        this.lruQueue = Collections.synchronizedList( new LinkedList<KeyValue>());
//        this.maxCacheSize = cacheSize;
//        this.disk = new Disk();
//    }
//
//    public String getValue(String key) throws Exception{
//        if (super._inCache(key)){
//            String value = super._getValue(key);
//            if (this.maxCacheSize == super._readCacheSize() ){
//                KeyValue kvToEvict = this.lruQueue.get(0);
//                boolean hasEvited = super._evict(kvToEvict);
//                // System.out.println("Removed " + key);
//            }
//            moveToFront(key);
//            return value;
//        } else {
//            String onDisk = this.disk.getFromDisk(key);
//            if (!onDisk.equals("null")){
//                if (this.maxCacheSize == super._readCacheSize() ){
//                    KeyValue kvToEvict = this.lruQueue.get(0);
//                    boolean hasEvited = super._evict(kvToEvict);
//                }
//                moveToFront(key);
//            }
//            return "null";
//        }
//    }
//
//    public boolean putKV(KeyValue KV){
//        try {
//            String key = KV.getKey();
//            String value = KV.getValue();
//            if (this.maxCacheSize == super._readCacheSize() ){
//                KeyValue kvToEvict = this.lruQueue.remove(0);
//                boolean hasEvited = super._evict(kvToEvict);
//            }
//            boolean hasAdded = super._putKeyValue(key, value);
//            if (hasAdded == false){
//                throw new Exception("Could not Add {"+ key + ":" + value + "} to the cache");
//            }
//            this.lruQueue.add(KV);
//            return true;
//        } catch (Exception e) {
//            //TODO: handle exception
//            return false;
//        }
//    }
//
//    private synchronized void moveToFront(String key) throws Exception{
//        if (super._inCache(key)){
//            String value = super._getValue(key);
//            KeyValue kvToMove = new KeyValue(key, value);
//            if (!lruQueue.get(lruQueue.size() - 1).getKey().equals(key)){
//                this.lruQueue.remove(kvToMove);
//                this.lruQueue.add(kvToMove);
//            }
//        } else {
//            String value = this.disk.getFromDisk(key);
//            KeyValue kvToMove = new KeyValue(key, value);
//            boolean hasAddedToCache = super._putKeyValue(key, value);
//            if (!lruQueue.get(lruQueue.size() - 1).getKey().equals(key)){
//                this.lruQueue.remove(kvToMove);
//                this.lruQueue.add(kvToMove);
//            }
//        }
//
//    }
//
//    public boolean inCache(String key){
//        return super._inCache(key);
//    }
//
//    public String onDisk(String key) throws Exception{
//        return this.disk.getFromDisk(key);
//    }
//
//    public Boolean clearCache() {
//        return super._clearCache();
//    }
//
//    public void printQueue() {
//        System.out.println("****** printing queue *********");
//        for (int i = 0; i < lruQueue.size(); i++) {
//            KeyValue kv = lruQueue.get(i);
//            System.out.println("<LRU Queue> {" + kv.getKey() + " : " + kv.getValue() + "}");
//        }
//        System.out.println("****** printing queue *********");
//    }
//
//}
