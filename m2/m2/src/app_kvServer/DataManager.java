package app_kvServer;

import app_kvServer.cache.CacheImplement;
import app_kvServer.cache.Disk;
import org.apache.log4j.Logger;

import java.util.LinkedList;

/**
 * Key for cache and disk is always the original key (no hash)
 */
public class DataManager {
    private Disk disk;
    private CacheImplement cacheImplement;
    private static Logger logger = Logger.getRootLogger();

    public DataManager(String cacheType, int cacheSize, String serverName){
        cacheImplement = new CacheImplement(cacheType, cacheSize);
        disk = new Disk(serverName);
    }

    public String get(String key) throws Exception {
        //get value from cache
        //first try to get value from cache
        if (this.cacheImplement.inCache(key)) {
            return cacheImplement.get(key);
        }
        String diskVal = disk.getFromDisk(key);
        if (diskVal == null || diskVal.equals("null")) {
            logger.info("ERROR: Key " + key +" not on Server");
            throw new Exception("Key not in server");
        }
        return diskVal;
    }

    /**
     *
     * @param key
     * @param value
     * @throws Exception if put is not success; returns nothing otherwise
     */
    public void put(String key, String value) throws Exception{
        this.disk.putOnDisk(key, value);
        try {
            this.cacheImplement.put(key,value);
        } catch (Exception e) {}
    }

    public void clearDataManager(){
        this.disk.clearDisk();
        this.cacheImplement.clearCache();
        logger.info("Clearing Disk and Cache Successful");
    }

    public boolean inDataManager(String key) {
        try{
            String diskVal = this.disk.getFromDisk(key);
            return this.cacheImplement.inCache(key) || (diskVal!=null && !diskVal.equals("null"));
        } catch (Exception e) {
            return false;
        }
    }

    public LinkedList<KeyValue> getAllDiskData(){
        return this.disk.getAllDiskData();
    }

}
