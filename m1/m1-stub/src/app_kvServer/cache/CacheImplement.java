package app_kvServer.cache;

import app_kvServer.KeyValue;
import app_kvServer.cache.Cache;
import app_kvServer.cache.FIFOCache;
import app_kvServer.cache.LRUCache;
import app_kvServer.cache.LFUCache;
import java.lang.Exception;

public class CacheImplement {
    // private enum Strategy {
    //     None,
    //     LRU,
    //     LFU,
    //     FIFO
    // };
	FIFOCache fifo;
	LRUCache lru;
	LFUCache lfu;
	private String type;
	Disk disk = new Disk();

	public CacheImplement(String type, int cacheSize){
		this.type = type;
		switch(type){
			case("FIFO"):
				// this.type = Strategy.FIFO;
				fifo = new FIFOCache(cacheSize);
			case("LRU"):
				// this.type = Strategy.LRU;
				lru = new LRUCache(cacheSize);
			case("LFU"):
				// this.type = Strategy.LFU;
				lfu = new LFUCache(cacheSize);
			// default:
				// this.type = Strategy.None;

		}
	}

	public String getValue(String key) throws Exception{
		switch(this.type){
			case("FIFO"):
				return fifo.getValue(key);
			case("LRU"):
				return lru.getValue(key);
			case("LFU"):
				return lfu.getValue(key);
			default:
				return "null";
		}
	}

	public boolean putKV(KeyValue KV){
		try {
			this.disk.putOnDisk(KV);
		} catch (Exception e) {
			System.out.println("in pkv in cimple - "+ e.toString());
			return false;
		}
		return true;
		// switch(this.type){
		// 	case("FIFO"):
		// 		return fifo.putKV(KV);
		// 	case("LRU"):
		// 		return lru.putKV(KV);
		// 	case("LFU"):
		// 		return lfu.putKV(KV);
		// 	default:
		// 		return false;
		// }
	}


	public String onDisk(String key) throws Exception{
		switch(this.type){
			case("FIFO"):
				return fifo.onDisk(key);
			case("LRU"):
				return lru.onDisk(key);
			case("LFU"):
				return lfu.onDisk(key);
			default:
				return null;
		}
	}

	public Boolean inCache(String key){
		switch(this.type){
			case("FIFO"):
				return fifo.inCache(key);
			case("LRU"):
				return lru.inCache(key);
			case("LFU"):
				return lfu.inCache(key);
			default:
				return null;
		}
	}

	public Boolean clearCache(){
		switch(this.type){
			case("FIFO"):
				return fifo.clearCache();
			case("LRU"):
				return lru.clearCache();
			case("LFU"):
				return lfu.clearCache();
			default:
				return null;
		}
	}

}