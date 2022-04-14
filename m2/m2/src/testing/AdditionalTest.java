package testing;

import client.KVStore;
import org.junit.Test;

import junit.framework.TestCase;

public class AdditionalTest extends TestCase {

	private KVStore kvClientFIFO;
	private KVStore kvClientLRU;
	private KVStore kvClientLFU;

	public void setUp(){
		kvClientFIFO = new KVStore("localhost", 50000);
		kvClientLFU = new KVStore("localhost", 50001);
		kvClientLRU = new KVStore("localhost", 50002);
		try {
			kvClientFIFO.connect();
			kvClientLRU.connect();
			kvClientLFU.connect();
		} catch (Exception e) {

		}
	}

	/*
	Scalability Tests
	 */
	@Test
	public void test_80puts_20gets(){
		Exception ex = null;
		System.out.println(" --------80% put / 20% get-----------");

		try {
			TestUtility.putXgetY(kvClientFIFO, 0.2, 5000, "FIFO");
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex == null);
	}

	@Test
	public void test_50puts_50gets(){
		Exception ex = null;
		System.out.println(" --------50% put / 50% get-----------");

		try {
			TestUtility.putXgetY(kvClientFIFO, 0.5, 5000, "FIFO");
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex == null);
	}

	@Test
	public void test_20puts_80gets(){
		Exception ex = null;
		System.out.println(" --------20% put / 80% get-----------");
		try {
			TestUtility.putXgetY(kvClientFIFO, 0.8, 5000, "FIFO");
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex == null);
	}

	@Test
	public void testStub() {
		assertTrue(true);
	}
}
