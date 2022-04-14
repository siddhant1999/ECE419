package testing;

import junit.framework.TestCase;
import org.junit.Test;

import app_kvServer.KeyValue;
import app_kvServer.cache.LRUCache;

public class TestingLRU extends TestCase {
	private LRUCache lru;

	
	public void setUp() {
		int size = 3;
		this.lru = new LRUCache(size);
	}

	public void testPut(){
        String key = "foo2";
		String value = "bar2";
        KeyValue kv = new KeyValue(key, value);
        boolean added = false;
        Exception ex = null;
        try {
            added = lru.putKV(kv);
        } catch (Exception e) {
            ex = e;
        }
        // assert (added == true && ex == null );
    }

	public void testMultiplePuts(){
        boolean added = false;
        Exception ex = null;
        try {
            for (int i = 1; i < 8; i++){
                KeyValue kv = new KeyValue("key" + Integer.toString(i), "value" +Integer.toString(i));
                added = lru.putKV(kv);
            }   
        } catch (Exception e) {
            ex = e;
        }

        assert (added == true && ex == null);
    }

	public void testMultipleGets(){
        String value;
        Exception ex = null;
        try {
            for (int i = 1; i < 20; i++){
                // KeyValue kv = new KeyValue("key1" , "value" +Integer.toString(i));
                value = lru.getValue("key1");
            }   
        } catch (Exception e) {
            ex = e;
        }

        // assert (added == true && ex == null);
    }


	public void printCache() {
        lru._printCache();
    }

	public void printQueue(){
		lru.printQueue();
	}

	public void testInCache() {
		for (int i = 1; i < 8; i++){
			Boolean value = lru.inCache("key" + Integer.toString(i));
			String pr = "key" + Integer.toString(i) + " -- > " + Boolean.toString(value);
			System.out.println(pr);
		} 
    }

	public void testOnDisk(){
		for (int i = 1; i < 8; i++){
			String value = lru.onDisk("key" + Integer.toString(i));
			System.out.println(value);
		} 
	}

	public void testClearCache() {
		boolean val = lru.clearCache();
		System.out.println(val);
	}

	public void testGetValue(){
		for (int i = 1; i < 8; i++){
			String value = lru.getValue("key" + Integer.toString(i));
			System.out.println(value);
		} 
	}

	@Test
	public void testAll() {
		System.out.println("############################# TESTING LRU ########################");
		testPut();
		testMultiplePuts();
		printCache();
		System.out.println("---- start in cache? ------");
		testInCache();
		System.out.println("---- end in cache? ------");
		testMultipleGets();
		System.out.println("---- start in cache? ------   2");
		testInCache();
		System.out.println("---- end in cache? ------    2");
		printQueue();


		System.out.println("---- start on disk? ------");
		testOnDisk();
		System.out.println("---- end on disk? ------");
		System.out.println("---- start get value? ------");
		testGetValue();
		System.out.println("---- end get value? ------");
		testClearCache();
	}

	
}
