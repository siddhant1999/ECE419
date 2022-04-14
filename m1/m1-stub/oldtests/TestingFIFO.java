package testing;
import org.junit.Test;

import app_kvServer.KeyValue;
import app_kvServer.cache.FIFOCache;

import junit.framework.TestCase;

public class TestingFIFO extends TestCase {
	private FIFOCache fifo;

	public void setUp() {
		int size = 4;
		this.fifo = new FIFOCache(size);
	}

	// @Test
	public void testPut(){
        String key = "foo2";
		String value = "bar2";
        KeyValue kv = new KeyValue(key, value);
        boolean added = false;
        Exception ex = null;
        try {
            added = fifo.putKV(kv);
        } catch (Exception e) {
            ex = e;
        }
        assert (added == true && ex == null );
    }

	// @Test
	public void testMultiplePuts(){
        boolean added = false;
        Exception ex = null;
        try {
            for (int i = 1; i < 8; i++){
                KeyValue kv = new KeyValue("key" + Integer.toString(i), "value" +Integer.toString(i));
                added = fifo.putKV(kv);
            }   
        } catch (Exception e) {
            ex = e;
        }

        assert (added == true && ex == null);
    }

	public void printCache() {
        fifo._printCache();
    }

	public void testInCache() {
		for (int i = 1; i < 8; i++){
			boolean value = fifo.inCache("key" + Integer.toString(i));
			System.out.println(value);
		} 
    }

	public void testOnDisk(){
		for (int i = 1; i < 8; i++){
			String value = fifo.onDisk("key" + Integer.toString(i));
			System.out.println(value);
		} 
	}

	public void testClearCache() {
		boolean val = fifo.clearCache();
		System.out.println(val);
	}

	public void testGetValue(){
		for (int i = 1; i < 8; i++){
			String value = fifo.getValue("key" + Integer.toString(i));
			System.out.println(value);
		} 
	}

	@Test
	public void testAll() {
		// TestingFIFO tf = new TestingFIFO();
		System.out.println("########################## TESTING FIFO ##########################");
		testPut();
		testMultiplePuts();
		printCache();
		System.out.println("---- start in cache? ------");
		testInCache();
		System.out.println("---- start in cache? ------");
		System.out.println("---- start on disk? ------");
		testOnDisk();
		System.out.println("---- end on disk? ------");
		System.out.println("---- start get value? ------");
		testGetValue();
		System.out.println("---- end get value? ------");
		testClearCache();
	}

	
}
