package testing;

import app_kvServer.KVServer;
import app_kvServer.KeyValue;
import client.KVStore;
import ecs.ECSNode;
import ecs.IECSNode;
import junit.framework.TestCase;
import org.junit.Test;
import shared.HashFunc;

import java.util.LinkedList;
import java.util.TreeMap;

public class TestMisc extends TestCase {

    KVServer s1,s2;

    public void setUp(){
        s1 = TestUtility.startServer(105, 3, "FIFO",false);
        s2 = TestUtility.startServer(106, 3, "FIFO", false);
    }


    @Test
    public void testPrintAllData(){
        KVStore kvStore = new KVStore("127.0.0.1", 105);
        boolean allDataCorrect = true;
        Exception ex = null;
        String s2Key = "zzxczxcasdfascrfw"; //known that it works on s2
        try {
            TreeMap<String, IECSNode> metadata = new TreeMap<String, IECSNode>();
            metadata.put(s1.hashedName, new ECSNode(s1.getHostname(), s1.getPort()));
            metadata.put(s2.hashedName, new ECSNode(s2.getHostname(), s2.getPort()));
            s1.setMetaData(new TreeMap<>(metadata));
            s2.setMetaData(new TreeMap<>(metadata));
            kvStore.serversMetaData = new TreeMap<>(metadata);
            kvStore.connect();

            kvStore.put("a", "1", false, false);
            kvStore.put("b", "2", false, false);
            kvStore.put("c", "3", false, false);
            kvStore.put("d", "4", false, false);
            s2.putKV("a","1", true, false, false); //should be on s1
            s2.putKV("b","2", true, false, false); // should be on s1
            s2.putKV(s2Key,"5", true, false, false);
            s1.putKV(s2Key,"5", true, false, false); // should be on s2


            kvStore.disconnect();
            kvStore.port = s2.getPort();
            kvStore.connect();

            //now print all data
            TreeMap<String, TreeMap<String, LinkedList<KeyValue>>> allData = kvStore.getAllData();

            allDataCorrect &= TestUtility.checkLLContainsKV(allData.get(s1.hashedName).get(s1.hashedName),
                    new KeyValue("a", "1"));
            allDataCorrect &= TestUtility.checkLLContainsKV(allData.get(s1.hashedName).get(s1.hashedName),
                    new KeyValue("b", "2"));
            allDataCorrect &= TestUtility.checkLLContainsKV(allData.get(s1.hashedName).get(s1.hashedName),
                    new KeyValue("c", "3"));
            allDataCorrect &= TestUtility.checkLLContainsKV(allData.get(s1.hashedName).get(s1.hashedName),
                    new KeyValue("d", "4"));
            allDataCorrect &= TestUtility.checkLLContainsKV(allData.get(s1.hashedName).get(s2.hashedName),
                    new KeyValue(s2Key, "5"));
            allDataCorrect &= TestUtility.checkLLContainsKV(allData.get(s2.hashedName).get(s2.hashedName),
                    new KeyValue(s2Key, "5"));
            allDataCorrect &= TestUtility.checkLLContainsKV(allData.get(s2.hashedName).get(s1.hashedName),
                    new KeyValue("a", "1"));
            allDataCorrect &= TestUtility.checkLLContainsKV(allData.get(s2.hashedName).get(s1.hashedName),
                    new KeyValue("b", "2"));

        } catch (Exception e) {
            ex = e;
            e.printStackTrace();
        }
        assertTrue(ex==null && allDataCorrect);
    }

    @Test
    public void testHashFuncNextLargest(){
        Exception ex = null;
        String n="1",nn="1",nnn="1";
        try{
            TreeMap<String, IECSNode> metadata = new TreeMap<>();
            metadata.put("a", null);
            metadata.put("b", null);
            metadata.put("c", null);

            n = HashFunc.findNextLargest("a", metadata);
            nn = HashFunc.findNextLargest(n, metadata);
            nnn = HashFunc.findNextLargest(nn, metadata);

        } catch (Exception e){
            ex = e;
        }
        assertTrue(ex==null && n.equals("b") && nn.equals("c") && nnn.equals("a"));
    }

    @Test
    public void testHashFuncPrev(){
        Exception ex = null;
        String p="1",pp="1",ppp="1";
        try{
            TreeMap<String, IECSNode> metadata = new TreeMap<>();
            metadata.put("a", null);
            metadata.put("b", null);
            metadata.put("c", null);

            p = HashFunc.findPrev("a", metadata);
            pp = HashFunc.findPrev(p, metadata);
            ppp = HashFunc.findPrev(pp, metadata);

        } catch (Exception e){
            ex = e;
        }
        assertTrue(ex==null && p.equals("c") && pp.equals("b") && ppp.equals("a"));
    }

}
