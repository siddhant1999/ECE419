package testing;

import app_kvServer.KeyValue;
import app_kvServer.cache.Disk;
import junit.framework.TestCase;
import org.junit.Test;


public class TestDisk extends TestCase {
	private Disk disk;
	public void setUp(){
		this.disk = new Disk("101");
	}

	@Test
	public void testPutGet() {
		String key = "foo";
		String value = "bar";
		Exception ex = null;
		String filePath = null;
		String getVal = null;
		try {
			filePath = this.disk.putOnDisk(key,value);
			getVal = this.disk.getFromDisk(key);

		} catch (Exception e) {
			ex = e; 
			System.out.println(("Error in test Put; res: " + filePath + " -"  + e.toString()));
		}
		assertTrue(ex == null && filePath.equals("filepath/101/f.txt") && getVal.equals(value));
	}

	@Test
	public void testPersistent() {
		String key = "alpha";
		String value = "beta";
		Exception ex = null;
		String res = null;
		try {
			res = this.disk.putOnDisk(key,value);
		} catch (Exception e) {
			ex = e; 
		}

		Disk newDisk = new Disk("101");
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
		String key = "fooReplace";
		String value = "omega";
		String value2 = "omega2";
		Exception ex = null;
		String res = null;
		try {
			res = this.disk.putOnDisk(key,value);

		} catch (Exception e) {
			ex = e; 
		}

		assertTrue(ex == null && res.equals("filepath/101/f.txt"));
		String retVal = null;
		try {
			res = this.disk.putOnDisk(key,value2);
			retVal = this.disk.getFromDisk(key);
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(retVal.equals(value2) && ex == null);
	}

	@Test
	public void testDelete() {
		String key = "foo";
		String value = "bar";
		Exception ex = null;
		String res = null;
		try {
			res = this.disk.putOnDisk(key,value);
		} catch (Exception e) {
			ex = e; 
			System.out.println(("Error in test Put; res: " + res + " -"  + e.toString()));
		}

		key = "foo";
		value = "null";

		try {
			this.disk.putOnDisk(key,value);
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

	public void tearDown() {
		this.disk.clearDisk();
	}


}
