package app_kvServer.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

import app_kvServer.KeyValue;
import java.lang.Exception;

public class Cache {
    private int cacheSize;
    private Map<String, String> hashMap;
    public Disk disk;

    public Cache(int cacheSize){
        this.cacheSize = cacheSize;
        this.hashMap = new ConcurrentHashMap<String, String>(this.cacheSize);
        this.disk = new Disk();
    }

    public String _getValue(String key){
        try {
            String res = this.hashMap.get(key);
            return res;
        } catch (Exception e) {
            //TODO IMPLEMETNT LOGGING
            return "null";
        }
    }

    public synchronized boolean _putKeyValue(String key, String value){
        try {
            this.hashMap.put(key, value);
            return true;
        } catch (Exception e) {
            // TODO IMPLEMENT LOGGING
            return false;
        }
    }

    public synchronized boolean _evict(KeyValue KV){
        // return false;
        try {
            String value = this.hashMap.remove(KV.getKey());
            if (value == null) {
                throw new Exception("The Key {" + KV.getKey() + "} is not an entry in the Cache");
            }
            this.disk.putOnDisk(KV);
            return true;
            
        } catch (Exception e) {
            //TODO: handle exception
            return false;
        }
    }

    public synchronized int _readCacheSize(){ 
        // We synch the read size to get the current size
        return this.hashMap.size();
    }

    // private boolean flushKV(){
    //     try {
            
    //     } catch (Exception e) {
    //         //TODO: handle exception
    //     }
    // }

    public synchronized boolean _inCache(String key){
        Set<String> keySet = this.hashMap.keySet();
        return keySet.contains(key) & this.hashMap.get(key)!="null";
    }

    public synchronized void _printCache(){
        Set<Map.Entry<String, String>> entrySet = this.hashMap.entrySet();
        Iterator<Map.Entry<String, String> > itr = entrySet.iterator();
        System.out.println("------------------ START CACHE ---------------------");
        while (itr.hasNext()){
            Map.Entry<String, String> entry = itr.next();
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("<PRINT CACHE> " + key + " : " + value);
        }
        System.out.println("------------------ END CACHE ---------------------");
    }

    public Boolean _clearCache() {
        try{
            this.hashMap.clear();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    
}