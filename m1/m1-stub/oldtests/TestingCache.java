package testing;

import client.KVStore;
import org.junit.Test;

import junit.framework.TestCase;
// import shared.messages.KVMessage;
// import shared.messages.KVMessage.StatusType;

// import app_kvClient.cache;
import app_kvServer.KeyValue;
import app_kvServer.cache.Cache;
import app_kvServer.cache.LRUCache;


public class TestingCache extends TestCase{
    private Cache lruCache;
    private KVStore kvClient;

    public void setUp(){
        int cacheSize = 3;
        lruCache = new Cache(cacheSize);
        kvClient = new KVStore("localhost", 50000);
    }

    @Test
    public void testPut(){
        String key = "foo2";
		String value = "bar2";
        KeyValue kv = new KeyValue(key, value);
        boolean added = false;
        Exception ex = null;
        try {
            added = lruCache._putKeyValue(key, value);
        } catch (Exception e) {
            ex = e;
        }
        assertTrue(added == true && ex == null );
    }

    // @Test
    public void testMultiplePuts(){
        boolean added = false;
        Exception ex = null;
        try {
            for (int i = 1; i < 8; i++){
                KeyValue kv = new KeyValue("key" + Integer.toString(i), "value" +Integer.toString(i));
                added = lruCache._putKeyValue(kv.getKey(), kv.getValue());
                assertTrue(added == true && ex == null);
            }   
        } catch (Exception e) {
            ex = e;
        }

        assertTrue(added == true && ex == null);
    }

    // private void printCache() {
    //     lruCache._printCache();
    // }

    // @Test
    // public void testAll() {
    //     System.out.println("######################### TESTING CACHE ####################");
    //     // setUp();
    //     testPut();
    //     testMultiplePuts();
    //     printCache();
	// }
}
