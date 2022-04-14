package testing;

import app_kvServer.KeyValue;
import app_kvServer.DataManager;
import junit.framework.TestCase;
import org.junit.Test;


public class TestDataManager extends TestCase {
//
//	private DataManager dataManager;
//
//	public void setUp() {
//		this.dataManager = new DataManager("FIFO", 123);
//	}
//
//	public void tearDown() {
//		this.dataManager.clearDataManager();
//	}
//
//	@Test
//	public void testPut(){
//		String key = "foo";
//		String value = "bar";
//		Exception ex = null;
//		String res = null;
//		try {
//			this.dataManager.putKV(key, value);
//		} catch (Exception e) {
//			ex = e;
//			System.out.println(("Error in test Put; res: " + res + " -"  + e.toString()));
//		}
//		assertTrue(ex == null);
//	}
//
//	@Test
//	public void testGet() {
//		testPut();
//
//		String key = "foo";
//		String value = "bar";
//		Exception ex = null;
//		String res = null;
//		try {
//			res = this.dataManager.getKV(key);
//		} catch (Exception e) {
//			ex = e;
//		}
//
//		assertTrue(ex == null && res.equals(value));
//	}
//
//	@Test
//	public void testPersistent() {
//		String key = "alpha";
//		String value = "beta";
//		KeyValue KV = new KeyValue(key, value);
//		Exception ex = null;
//		String res = null;
//		try {
//			this.dataManager.putKV(key,value);
//		} catch (Exception e) {
//			ex = e;
//		}
//
//		DataManager newDM = new DataManager("FIFO", 123);
//		String retValue = null;
//
//		try {
//			retValue = newDM.getKV(key);
//		} catch (Exception e) {
//			ex = e;
//		}
//
//		assertTrue(retValue.equals(value) && ex == null);
//	}
//
//	@Test
//	public void testReplace() {
//		testPut();
//
//		String key = "foo";
//		String value = "omega";
//		Exception ex = null;
//		String res = null;
//		try {
//			this.dataManager.putKV(key,value);
//		} catch (Exception e) {
//			ex = e;
//			System.out.println("exception in putting:  " + e.toString());
//		}
//		// System.out.println("response from c" +res));
//
//		// assertTrue(ex == null && res.equals("filepath/f.txt"));
//		String retVal = null;
//		try {
//			retVal = this.dataManager.getKV(key);
//		} catch (Exception e) {
//			ex = e;
//		}
//		assertTrue(retVal.equals(value) && ex == null);
//	}
//
//
//	@Test
//	public void testDelete() {
//		String key = "foo";
//		String value = "bar";
//		Exception ex = null;
//		String res = null;
//		try {
//			this.dataManager.putKV(key,value);
//		} catch (Exception e) {
//			ex = e;
//			System.out.println(("Error in test Put; res: " + res + " -"  + e.toString()));
//		}
//
//		key = "foo";
//		value = "null";
//
//		try {
//			this.dataManager.putKV(key, value);
//		} catch (Exception e) {
//			ex = e;
//		}
//
//		String getNull = null;
//		try {
//			getNull = this.dataManager.getKV(key);
//		} catch (Exception e) {
//			System.out.println("exception in get null " + getNull + "e: " + e.toString());
//		}
//
//		assertTrue(getNull == null && ex == null);
//
//	}
}
