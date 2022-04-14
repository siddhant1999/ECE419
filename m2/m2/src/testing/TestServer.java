package testing;

import app_kvServer.KVServer;
import app_kvServer.KeyValue;
import ecs.ECSNode;
import ecs.IECSNode;
import junit.framework.TestCase;
import org.junit.Test;
import shared.HashFunc;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

public class TestServer extends TestCase{

    KVServer server, serverDelete, serverDeleteAll,serverPersistence;


    public void setUp(){
        server = new KVServer(102, 3, "FIFO", false);
        //need to update metadata
        server.initKVServer(TestUtility.generateOwnMetadata(server));
		server.startServer();

		serverDelete = new KVServer(130, 3, "FIFO", false);
		//need to update metadata
		serverDelete.initKVServer(TestUtility.generateOwnMetadata(serverDelete));
		serverDelete.startServer();
		serverDeleteAll = new KVServer(131, 3, "FIFO", false);
		//need to update metadata
		serverDeleteAll.initKVServer(TestUtility.generateOwnMetadata(serverDeleteAll));
		serverDeleteAll.startServer();

		serverPersistence = new KVServer(132, 3, "FIFO", false);
		//need to update metadata
		serverPersistence.initKVServer(TestUtility.generateOwnMetadata(serverPersistence));
		serverPersistence.startServer();
	}

    public void tearDown(){
		server.clearStorage();
		//no need to close server since no ports are opened
    }

    @Test
	public void testPutGet() {
		String key = "foo";
		String value = "bar";
        String getValue = null;
		Exception ex = null;
		try {
			server.putKV(key, value, true, false, false);
            getValue = server.getKV(key);
		} catch (Exception e) {
			ex = e;
			System.out.println(("Error in testPutKV;" + e.toString()));
		}
		assertTrue(ex == null && getValue!=null && getValue.equals(value));
	}

	@Test
	public void testGetError(){
		String key = "NonExistentKey";
		Exception ex = null;
		String getVal = null;
		try {
			getVal = server.getKV(key);
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex.getMessage().equals("Key not in server") && getVal==null);
	}

    @Test
	public void testDelete() {
		String key = "fooDelete";
		String value = "value";
		Exception ex = null;
		try {
            this.server.putKV(key, value, false, false, false);
            this.server.putKV(key, "null", false, false, false);
		} catch (Exception e) {
			System.out.println(("Error in testDelete;" + e.toString()));
		}
		try {
			value = this.server.getKV("foo");
		} catch (Exception e) {
			ex = e;
		}
		assertTrue(ex.getMessage().equals("Key not in server"));
	}

	@Test
	public void testUpdate() {
		String key = "fooUpdate";
        String value = "value";
        String value2 = "value2";
        String getVal = null;
		Exception ex = null;
		try {
            this.server.putKV(key, value, false, false, false);
            this.server.putKV(key, value2, false, false, false);
            getVal = this.server.getKV("fooUpdate");
		} catch (Exception e) {
            ex = e;
		}
		assertTrue(ex==null && getVal!=null && getVal.equals(value2));
	}

	@Test
	public void testMoveData(){
		LinkedList<KeyValue> kvPairs1 = new LinkedList<>();
		LinkedList<KeyValue> kvPairs2 = new LinkedList<>();
		Boolean correct1 = true, correct2 = true;
		Exception ex = null;
		try {
			//first put data into server1
			LinkedList<String>ll = new LinkedList<String>(List.of("a", "b", "c", "d", "e"));

			for (int i = 0 ; i < ll.size(); i++) {
				testing.AllTests.TestServer_Server1.putKV(ll.get(i), Integer.toString(i), false, false, false);
			}
			//update metadata to now have second server
			TreeMap<String, IECSNode> newServer1Metadata = TestUtility.generateOwnMetadata(testing.AllTests.TestServer_Server1);
			newServer1Metadata.put(testing.AllTests.TestServer_Server2.hashedName,
					new ECSNode(testing.AllTests.TestServer_Server2.getHostname(),
							testing.AllTests.TestServer_Server2.getPort()));
			testing.AllTests.TestServer_Server1.setMetaData(new TreeMap<>(newServer1Metadata));
			testing.AllTests.TestServer_Server2.setMetaData(new TreeMap<>(newServer1Metadata));

			//move data from server1 to server2
			testing.AllTests.TestServer_Server1.moveData(testing.AllTests.TestServer_Server2.hashedName,
					testing.AllTests.TestServer_Server2.hashedName, true, false);

			kvPairs1 = TestUtility.getPresentKeys(testing.AllTests.TestServer_Server1, ll);
			kvPairs2 = TestUtility.getPresentKeys(testing.AllTests.TestServer_Server2, ll);

			// server1 103: e
			// server2 104: a, b, c, d
			LinkedList<KeyValue> kv1 = new LinkedList<KeyValue>(List.of(new KeyValue("e", "4")));
			LinkedList<KeyValue> kv2 = new LinkedList<KeyValue>(List.of(new KeyValue("a", "0"),
					new KeyValue("b", "1"), new KeyValue("c", "2"), new KeyValue("d", "3")));
			for(KeyValue kv : kv1){
				correct1 &= (TestUtility.checkLLContainsKV(kvPairs1,kv) &
						!TestUtility.checkLLContainsKV(kvPairs2,kv));
			}
			for(KeyValue kv : kv2){
				correct2 &= (TestUtility.checkLLContainsKV(kvPairs2,kv) &
						!TestUtility.checkLLContainsKV(kvPairs1,kv));
			}

		} catch (Exception e) {
			e.printStackTrace();
			ex = e;
		}
		assertTrue(correct1 && correct2 && ex==null);
	}

	@Test
	public void testDeleteData(){

		Exception ex = null, ex1=null, ex2=null,ex3=null;
		String v1,v2,v3;
		try{
			serverDelete.putKV("a", "a", true, false, false);
			serverDelete.putKV("b", "b", true, false, false);
			serverDelete.putKV("c", "c", true, false, false);
			TreeMap<String, IECSNode> metadata = new TreeMap<>();
			metadata.put("5", null);
			metadata.put("93", null);
			serverDelete.setMetaData(metadata);
			serverDelete.deleteData("5");
			serverDelete.setMetaData(TestUtility.generateOwnMetadata(serverDelete));
			try{
				v1 = serverDelete.getKV("a");

			} catch(Exception e){
				ex1 = e;
			}
			try{
				v2 = serverDelete.getKV("b");
			} catch(Exception e){
				ex2 = e;
			}
			try{
				v3 = serverDelete.getKV("c");
			} catch(Exception e){
				ex3 = e;
			}
		}catch(Exception e){
			ex = e;
		}
		assertTrue(ex==null && ex2==null && ex1.getMessage().equals("Key not in server") &&
				ex3.getMessage().equals("Key not in server"));
	}

	@Test
	public void testDeleteAllData(){

		Exception ex = null, ex1=null, ex2=null,ex3=null;
		String v1,v2,v3;
		try{
			serverDelete.putKV("a", "a", true, false, false);
			serverDelete.putKV("b", "b", true, false, false);
			serverDelete.putKV("c", "c", true, false, false);
			serverDelete.deleteData("");
			try{
				v1 = serverDelete.getKV("a");
			} catch(Exception e){
				ex1 = e;
			}
			try{
				v2 = serverDelete.getKV("b");
			} catch(Exception e){
				ex2 = e;
			}
			try{
				v3 = serverDelete.getKV("c");
			} catch(Exception e){
				ex3 = e;
			}
		}catch(Exception e){
			ex = e;
		}
		assertTrue(ex==null &&
				ex1.getMessage().equals("Key not in server") &&
				ex1.getMessage().equals("Key not in server") &&
				ex3.getMessage().equals("Key not in server"));
	}

	@Test
	public void testPersistence(){
		Exception ex = null;
		String v1="1",v2="1",v3="1";
		try {
			serverPersistence.putKV("a", "a", true, false, false);
			serverPersistence.putKV("b", "b", true, false, false);
			serverPersistence.putKV("c", "c", true, false, false);
			serverPersistence.close();
			serverPersistence = null;
			serverPersistence = new KVServer(132, 3, "FIFO", false);
			//need to update metadata
			serverPersistence.initKVServer(TestUtility.generateOwnMetadata(serverPersistence));
			serverPersistence.startServer();

			v1=serverPersistence.getKV("a");
			v2=serverPersistence.getKV("b");
			v3=serverPersistence.getKV("c");
		} catch (Exception e){
			ex = e;
		}
		assertTrue(ex==null && v1.equals("a") && v2.equals("b") && v3.equals("c"));
	}


}
