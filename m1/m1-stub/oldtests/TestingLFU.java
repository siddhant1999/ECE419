package testing;
import java.io.Console;

import junit.framework.TestCase;
import org.junit.Test;

import app_kvServer.KeyValue;
import app_kvServer.cache.LFUCache;

public class TestingLFU extends TestCase {


	private LFUCache lfu;

	public void setUp() {
		int size = 4;
		this.lfu = new LFUCache(size);
	}


	@Test
	public void testPut(){
        String key = "foo2";
		String value = "bar2";
        KeyValue kv = new KeyValue(key, value);
        boolean added = false;
        Exception ex = null;
        try {
            added = lfu.putKV(kv);
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
			int i = 1;
            for (i = 1; i < 8; i++){
                KeyValue kv = new KeyValue("key" + Integer.toString(i), "value" +Integer.toString(i));
                added = lfu.putKV(kv);
            }   
			// KeyValue kv = new KeyValue("key" + Integer.toString(i), "value" +Integer.toString(i));
			// added = lfu.putKV(kv);

        } catch (Exception e) {
            ex = e;
        }

        assertTrue(added == true && ex == null);
    }

	// @Test
	public void testMultipleGets(){
        String value;
        Exception ex = null;
        try {
            for (int i = 1; i < 20; i++){
                // KeyValue kv = new KeyValue("key1" , "value" +Integer.toString(i));
                value = lfu.getValue("key1");
            }   
        } catch (Exception e) {
            ex = e;
        }

        // assert (added == true && ex == null);
    }


	public void printCache() {
        lfu._printCache();
    }

	public void printLFU(){
		lfu.printLFU();
	}

	// @Test
	public void testInCache() {
		for (int i = 1; i < 8; i++){
			Boolean value = lfu.inCache("key" + Integer.toString(i));
			String pr = "key" + Integer.toString(i) + " -- > " + Boolean.toString(value);
			System.out.println(pr);
		} 
    }

	// @Test
	public void testOnDisk(){
		for (int i = 1; i < 8; i++){
			String value = lfu.onDisk("key" + Integer.toString(i));
			System.out.println(value);
		} 
	}

	// @Test
	public void testClearCache() {
		boolean val = lfu.clearCache();
		System.out.println(val);
	}

	// @Test
	public void testGetValue(){
		for (int i = 1; i < 8; i++){
			String value = lfu.getValue("key" + Integer.toString(i));
			System.out.println(value);
		} 
	}

	@Test
	public void testAll() {
		System.out.println("######################## TESTING LFU ###########################");
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
		printLFU();


		System.out.println("---- start on disk? ------");
		testOnDisk();
		System.out.println("---- end on disk? ------");
		System.out.println("---- start get value? ------");
		testGetValue();
		System.out.println("---- end get value? ------");
		testClearCache();
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ENDEND TESTING LFU %%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

	}

	
}
