package app_kvServer.cache;

import app_kvServer.KeyValue;

import java.lang.Exception;

public class CacheImplement {
	ICache cache;
	private String type;

	public CacheImplement(String type, int cacheSize){
		this.type = type;
		cache = new FIFOCache(cacheSize);
		//only 1 cache type implemented
//		switch(type){
//			case("FIFO"):
//				// this.type = Strategy.FIFO;
//				fifo = new FIFOCache(cacheSize);
//			case("LRU"):
//				// this.type = Strategy.LRU;
//				lru = new LRUCache(cacheSize);
//			case("LFU"):
//				// this.type = Strategy.LFU;
//				lfu = new LFUCache(cacheSize);
//			// default:
//				// this.type = Strategy.None;
//
//		}
	}

	public String get(String key) throws Exception{
		if (!cache.inCache(key)) throw new Exception ("Key not in cache");
		return cache.get(key);
	}

	public void put(String key, String value){
		/*
		Only error that occurs in cache in put delete a non existent value
		-> but this will occur naturally on data replication/moving
		-> error is not important as long as data written to disk
		-> hence no error return for put
		 */
		cache.put(key,value);
	}

	public Boolean inCache(String key){
		return cache.inCache(key);
	}

	public void clearCache(){
		cache.clearCache();
	}


}