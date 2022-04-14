package testing;

import app_kvServer.KeyValue;
import app_kvServer.cache.Disk;
import junit.framework.TestCase;
import org.junit.Test;


public class TestDisk extends TestCase {
	private Disk disk;

	public void setUp(){
		this.disk = new Disk();
	}

	@Test
	public void testPut() {
		String key = "foo";
		String value = "bar";
		KeyValue KV = new KeyValue(key, value);
		Exception ex = null;
		String res = null;
		try {
			res = this.disk.putOnDisk(KV);
		} catch (Exception e) {
			ex = e; 
			System.out.println(("Error in test Put; res: " + res + " -"  + e.toString()));
		}
		// System.out.println("test Put ---> " + res);
		assertTrue(ex == null && res.equals("filepath/f.txt"));
	}

	@Test
	public void testGet(){
		testPut();
		
		String key = "foo";
		String value = "bar";
		KeyValue KV = new KeyValue(key, value);
		Exception ex = null;
		String res = null;
		try {
			// String res = this.disk.putOnDisk(KV)
			res = this.disk.getFromDisk(key);
		} catch (Exception e) {
			ex = e; 
		}

		assertTrue(ex == null && res.equals(value));
	}

	@Test
	public void testPersistent() {
		String key = "alpha";
		String value = "beta";
		KeyValue KV = new KeyValue(key, value);
		Exception ex = null;
		String res = null;
		try {
			res = this.disk.putOnDisk(KV);
		} catch (Exception e) {
			ex = e; 
		}

		Disk newDisk = new Disk();
		String retValue = null;

		try {
			retValue = newDisk.getFromDisk(key);
		} catch (Exception e) {
			ex = e;
		}

		assertTrue(retValue.equals(value) && ex == null);

	}

	@Test
	public void testReplace() {
		testPut();

		String key = "foo";
		String value = "omega";
		KeyValue KV = new KeyValue(key, value);
		Exception ex = null;
		String res = null;
		try {
			res = this.disk.putOnDisk(KV);
		} catch (Exception e) {
			ex = e; 
		}
		// System.out.println("response from c" +res));
		
		assertTrue(ex == null && res.equals("filepath/f.txt"));
		String retVal = null;
		try {
			retVal = this.disk.getFromDisk(key);
		} catch (Exception e) {
			ex = e;
		}
		// System.out.println("ret val   " + retVal);
		assertTrue(retVal.equals(value) && ex == null);
	}

	@Test
	public void testDelete() {
		String key = "foo";
		String value = "bar";
		KeyValue KV = new KeyValue(key, value);
		Exception ex = null;
		String res = null;
		try {
			res = this.disk.putOnDisk(KV);
		} catch (Exception e) {
			ex = e; 
			System.out.println(("Error in test Put; res: " + res + " -"  + e.toString()));
		}

		key = "foo";
		value = "null";
		KV = new KeyValue(key, value);
		String fname = null;

		try {
			fname = this.disk.putOnDisk(KV);
		} catch (Exception e) {
			ex = e;
		}

		String getNull = null;
		try {
			getNull = this.disk.getFromDisk(key);
		} catch (Exception e) {
			System.out.println("exception in get null " + getNull + "e: " + e.toString());
		}

		assertTrue(getNull == null && ex == null);

	}


}
