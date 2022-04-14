package testing;

import app_kvServer.KVServer;
import app_kvServer.KeyValue;
import client.KVStore;
import ecs.ECSNode;
import ecs.IECSNode;
import junit.framework.TestCase;
import org.junit.Test;
import shared.HashFunc;
import shared.messages.KVMessage;

import java.util.LinkedList;
import java.util.TreeMap;

public class TestClientServerReplication extends TestCase {
    KVServer s1,s2, s3, s4;
    TreeMap<String, IECSNode> metadata;

    public void setUp(){
        //need to update metadata
        s1 = TestUtility.startServer(120, 3, "FIFO", false);
        s2 = TestUtility.startServer(121, 3, "FIFO", false);
        s3 = TestUtility.startServer(122, 3, "FIFO", false);
        s4 = TestUtility.startServer(123, 3, "FIFO", false);
        metadata = new TreeMap<String, IECSNode>();
        metadata.put(s1.hashedName, new ECSNode(s1.getHostname(), s1.getPort()));
        metadata.put(s2.hashedName, new ECSNode(s2.getHostname(), s2.getPort()));
        metadata.put(s3.hashedName, new ECSNode(s3.getHostname(), s3.getPort()));
        metadata.put(s4.hashedName, new ECSNode(s4.getHostname(), s4.getPort()));
        s1.setMetaData(metadata);
        s2.setMetaData(metadata);
        s3.setMetaData(metadata);
        s4.setMetaData(metadata);
    }

    @Test
    public void testReplicationPut(){
        KVStore kvStore = new KVStore("127.0.0.1", 120);
        Exception ex = null;
        boolean dataCorrect = true;
        try {
            kvStore.connect();
            kvStore.put("a", "1", false, true);
            kvStore.put("e", "2", false, true);

            TreeMap<String, TreeMap<String, LinkedList<KeyValue>>> allData = kvStore.getAllData();
            KeyValue kv1 = new KeyValue("a", "1");
            KeyValue kv2 = new KeyValue("e", "2");
            dataCorrect &= TestUtility.inAllDataLL(allData, s4.hashedName, s4.hashedName, kv1);
            dataCorrect &= TestUtility.inAllDataLL(allData, s2.hashedName, s4.hashedName, kv1);
            dataCorrect &= TestUtility.inAllDataLL(allData, s3.hashedName, s4.hashedName, kv1);
            dataCorrect &= TestUtility.inAllDataLL(allData, s3.hashedName, s3.hashedName, kv2);
            dataCorrect &= TestUtility.inAllDataLL(allData, s4.hashedName, s3.hashedName, kv2);
            dataCorrect &= TestUtility.inAllDataLL(allData, s1.hashedName, s3.hashedName, kv2);
        } catch (Exception e){
            ex = e;
            e.printStackTrace();
        }
        assertTrue(ex==null && dataCorrect);
    }

    @Test
    public void testReplicationUpdate(){
        KVStore kvStore = new KVStore("127.0.0.1", 120);
        Exception ex = null;
        boolean dataCorrect = true;
        try {
            kvStore.connect();
            kvStore.put("b", "1", false, true);
            kvStore.put("b", "2", false, true);

            TreeMap<String, TreeMap<String, LinkedList<KeyValue>>> allData = kvStore.getAllData();
            KeyValue kv1 = new KeyValue("b", "2");
            String n = HashFunc.findNextLargest(s4.hashedName, metadata);
            String nn = HashFunc.findNextLargest(n, metadata);
            dataCorrect &= TestUtility.inAllDataLL(allData, s4.hashedName, s4.hashedName, kv1);
            dataCorrect &= TestUtility.inAllDataLL(allData, n, s4.hashedName, kv1);
            dataCorrect &= TestUtility.inAllDataLL(allData, nn, s4.hashedName, kv1);

        } catch (Exception e){
            ex = e;
            e.printStackTrace();
        }
        assertTrue(ex==null && dataCorrect);
    }

    @Test
    public void testReplicationDelete(){
        KVStore kvStore = new KVStore("127.0.0.1", 120);
        Exception ex = null;
        boolean dataCorrect = true;
        try {
            kvStore.connect();
            kvStore.put("c", "1", false, true);
            kvStore.put("c", "null", false, true);

            TreeMap<String, TreeMap<String, LinkedList<KeyValue>>> allData = kvStore.getAllData();

            TestUtility.printAllData(allData, metadata, "TestReplication");



            KeyValue kv1 = new KeyValue("c", "1");
            String n = HashFunc.findNextLargest(s4.hashedName, metadata);
            String nn = HashFunc.findNextLargest(n, metadata);
            dataCorrect &= !TestUtility.inAllDataLL(allData, s4.hashedName, s4.hashedName, kv1);
            dataCorrect &= !TestUtility.inAllDataLL(allData, n, s4.hashedName, kv1);
            dataCorrect &= !TestUtility.inAllDataLL(allData, nn, s4.hashedName, kv1);

        } catch (Exception e){
            ex = e;
            e.printStackTrace();
        }
        assertTrue(ex==null && dataCorrect);
    }
}
