package testing;

import org.apache.log4j.Level;
import org.junit.Test;

import client.KVStore;
import junit.framework.TestCase;
import shared.messages.KVMessage;
import shared.messages.KVMessage.StatusType;


public class InteractionTest extends TestCase {

	private KVStore kvClient;
	
	public void setUp() {
		kvClient = new KVStore("localhost", 50000);
		kvClient.logger.setLevel(Level.OFF);
		try {
			kvClient.connect();
		} catch (Exception e) {
		}
	}

	public void tearDown() {
		kvClient.disconnect();
	}
	

	@Test
	public void testPut() {
		String key = "foo2";
		String value = "bar2";
		KVMessage response = null;
		Exception ex = null;

		try {
			response = kvClient.put(key, value);
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex == null && response.getStatus() == StatusType.PUT_SUCCESS);
	}

	@Test
	public void testPutDisconnected() {
		/**
		 * This test is expected to output error "Error! Connection Lost!" when logger set to all
		 */
		kvClient.disconnect();
		String key = "foo";
		String value = "bar";
		Exception ex = null;
		Exception ex2 = null;
		Exception ex3 = null;

		try {
			kvClient.put(key, value);
		} catch (Exception e) {
			ex = e;
		}
		try {
			kvClient.put(key, "null");
		} catch (Exception e) {
			ex2 = e;
		}
		try {
			kvClient.get(key);
		} catch (Exception e) {
			ex3 = e;
		}

		assertNotNull(ex!=null && ex2!=null && ex3!=null);
	}

	@Test
	public void testUpdate() {
		String key = "updateTestValue";
		String initialValue = "initial";
		String initialRetrievedValue = "";
		String updatedValue = "updated";
		String updatedRetrievedValue = "";
		KVMessage response = null;
		Exception ex = null;

		try {
			kvClient.put(key, initialValue);
			initialRetrievedValue = kvClient.get(key).getValue();
			response = kvClient.put(key, updatedValue);
			updatedRetrievedValue = kvClient.get(key).getValue();
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex == null && response.getStatus() == StatusType.PUT_UPDATE
				&& response.getValue().equals(updatedValue)
				&& updatedRetrievedValue.equals(updatedValue)
				&& initialRetrievedValue.equals(initialValue)
		);
	}

	@Test
	public void testDelete() {
		String key = "deleteTestValue";
		String value = "toDelete";

		KVMessage response = null;
		KVMessage getResponse = null;
		Exception ex = null;

		try {
			kvClient.put(key, value);
			response = kvClient.put(key, "null");
			getResponse = kvClient.get(key);
		} catch (Exception e) {
			ex = e;
		}

		assertTrue(ex == null && response.getStatus() == StatusType.DELETE_SUCCESS
		&& getResponse.getStatus() == StatusType.GET_ERROR);
	}

	@Test
	public void testDeleteUnsetValue() {
		String key = "deleteUnsetTestValue";
		KVMessage response = null;
		Exception ex = null;

		try {
			response = kvClient.put(key, "null");
		} catch (Exception e) {
			ex = e;
		}

		assertTrue(ex == null && response.getStatus() == StatusType.DELETE_ERROR);
	}

	@Test
	public void testGet() {
		String key = "foo";
		String value = "bar";
		KVMessage response = null;
		Exception ex = null;

			try {
				kvClient.put(key, value);
				response = kvClient.get(key);
			} catch (Exception e) {
				ex = e;
			}

		assertTrue(ex == null && response.getValue().equals("bar"));
	}

	@Test
	public void testGetUnsetValue() {
		String key = "an unset value";
		KVMessage response = null;
		Exception ex = null;

		try {
			response = kvClient.get(key);
		} catch (Exception e) {
			ex = e;
		}

		assertTrue(ex == null && response.getStatus() == StatusType.GET_ERROR);
	}



}
