package testing;

import app_kvServer.KVServer;
import ecs.ECSNode;
import ecs.IECSNode;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.TreeMap;

public class Test3ServerReplication extends TestCase {
    KVServer s1,s2, s3;
    TreeMap<String, IECSNode> metadata;

    public void setUp(){
        s1 = TestUtility.startServer(107, 3, "FIFO", false);
        s2 = TestUtility.startServer(108, 3, "FIFO", false);
        s3 = TestUtility.startServer(109, 3, "FIFO", false);
        metadata = new TreeMap<String, IECSNode>();
        metadata.put(s1.hashedName, new ECSNode(s1.getHostname(), s1.getPort()));
        metadata.put(s2.hashedName, new ECSNode(s2.getHostname(), s2.getPort()));
        metadata.put(s3.hashedName, new ECSNode(s3.getHostname(), s3.getPort()));
        s1.setMetaData(metadata);
        s2.setMetaData(metadata);
        s3.setMetaData(metadata);
    }

    @Test
    public void testReplicatePut(){
        Exception ex = null;
        String val2 = "2", val3 = "3";
        try{
            s1.putKV("put", "1", true, true, false);
            val2 = s2.getKV("put");
            val3 = s3.getKV("put");
        } catch (Exception e) {
            ex = e;
            ex.printStackTrace();

        }
        assertTrue(ex == null && val2.equals("1") && val3.equals("1"));
    }

    @Test
    public void testReplicatePutUpdate(){
        Exception ex = null;
        String val2 = "3", val3 = "3";
        try{
            s1.putKV("putUpdate", "1", true, true, false);
            s1.putKV("putUpdate", "2", true, true, false);
            val2 = s2.getKV("putUpdate");
            val3 = s3.getKV("putUpdate");
        } catch (Exception e) {
            ex = e;
            ex.printStackTrace();

        }
        assertTrue(ex == null && val2.equals("2") && val3.equals("2"));
    }

    @Test
    public void testReplicatePutDelete(){
        Exception ex=null,ex2=null,ex3 = null;
        String val2 = "3", val3 = "3";
        try{
            s1.putKV("putDelete", "1", true, true, false);
            s1.putKV("putDelete", "null", true, true, false);
            try{
                val2 = s2.getKV("putUpdate");
            } catch (Exception e) {
                ex2 = e;
            }
            try{
                val3 = s3.getKV("putUpdate");
            } catch (Exception e) {
                ex3 = e;
            }
        } catch (Exception e) {
            ex = e;
            ex.printStackTrace();

        }
        assertTrue(ex == null && ex2!=null && ex3!=null &&
                ex2.getMessage().equals("Key not in server") &&
                ex3.getMessage().equals("Key not in server"));
    }

    @Test
    public void testReplicateUnsetGet(){
        Exception ex1=null,ex2=null,ex3=null;

        try{
            s1.getKV("unsetKey");
        } catch (Exception e) {
            ex1 = e;
        }
        try{
            s2.getKV("unsetKey");
        } catch (Exception e) {
            ex2 = e;
        }
        try{
            s3.getKV("unsetKey");
        } catch (Exception e) {
            ex3 = e;
        }

        assertTrue(ex1.getMessage().equals("Key not in server") &&
                ex2.getMessage().equals("Key not in server") &&
                ex3.getMessage().equals("Key not in server"));
    }

    @Test
    public void testReplicateUnsetDelete(){
        Exception ex1=null,ex2=null,ex3=null;

        try{
            s1.putKV("unsetKey", "null", false, false, false);
        } catch (Exception e) {
            ex1 = e;
        }
        try{
            s2.putKV("unsetKey", "null", false, false, false);
        } catch (Exception e) {
            ex2 = e;
        }
        try{
            s3.putKV("unsetKey", "null", false, false, false);
        } catch (Exception e) {
            ex3 = e;
        }
        assertTrue(ex1.getMessage().equals("Unable to Delete, Key not in Disk") &&
                ex2.getMessage().equals("Unable to Delete, Key not in Disk") &&
                ex3.getMessage().equals("Unable to Delete, Key not in Disk"));
    }
}
