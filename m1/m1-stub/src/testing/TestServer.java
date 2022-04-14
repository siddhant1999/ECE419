package testing;

import app_kvServer.KVServer;
import app_kvServer.KeyValue;
// import app_kvServer.cache.Disk;
import junit.framework.TestCase;
import org.junit.Test;

public class TestServer extends TestCase{
	KVServer server;
	public void setUp(){
		try {
			this.server = new KVServer(5000, 0, "FIFO");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testPutKV() {
		String key = "foo";
		String value = "bar";

		Exception ex = null;
		try {
			this.server.putKV(key, value);
		} catch (Exception e) {
			ex = e; 
			System.out.println(("Error in testPutKV;" + e.toString()));
		}
		assertTrue(ex == null);
	}

	@Test
	public void testGetKV() {
		testPutKV();
		Exception ex = null;
		String value = null;

		try {
			value = this.server.getKV("foo");
		} catch (Exception e) {
			ex = e; 
			System.out.println(("Error in testGetKV;" + e.toString()));
		}
		assertTrue(ex == null && value.equals("bar"));
	}

	@Test
	public void testDelete() {
		testPutKV();

		String key = "foo";
		String value = null;

		Exception ex = null;

		try {
			this.server.putKV(key, value);
		} catch (Exception e) {
			System.out.println(("Error in testDelete;" + e.toString()));
		}
		
		try {
			value = this.server.getKV("foo");
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex.getMessage().equals("Key not found in Cache or Disk"));
	}

	@Test
	public void testUpdate() {
		testPutKV();

		String key = "foo";
		String value = "new_bar";

		Exception ex = null;

		try {
			this.server.putKV(key, value);
		} catch (Exception e) {
			System.out.println(("Error in testUpdate;" + e.toString()));
		}
		
		try {
			value = this.server.getKV("foo");
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex==null && value.equals("new_bar"));
	}


}
