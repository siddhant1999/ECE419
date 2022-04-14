package testing;

import app_kvECS.ECSClient;
import app_kvServer.KVServer;
import client.KVStore;
import ecs.IECSNode;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class PerformanceTest extends TestCase {

    ECSClient ecs1, ecs2;
    public void setUp(){
        ecs1 = new ECSClient(false);
        ecs2 = new ECSClient(false);
    }


    @Test
    public void testPerformanceClientsServers(){
        LinkedList<Integer> numClients = new LinkedList<>();
        LinkedList<Integer> numServers = new LinkedList<>();
        numClients.add(1);numClients.add(5);
        numServers.add(1);numServers.add(5);
        Random rn = new Random();

        for (Integer ic : numClients) {
            for (Integer is : numServers) {
                if(ic==1 && is == 1) continue;
                LinkedList<KVServer> servers = TestUtility.startNServers(is, 100, "FIFO", false);
                LinkedList<KVStore> clients = TestUtility.startNClient(ic);
                //first set all clients to same port
                for(KVStore client : clients){
                    client.port = 20000;
                    try{
                        client.connect();
                    } catch (Exception e) {
                        System.out.println("Cannot connect client to port " + client.port);
                        e.printStackTrace();
                    }
                }
                //all servers are already starting
                LinkedList<String> keys = TestUtility.getKeys();
                LinkedList<String> vals = TestUtility.getKeys();
                int minimum = 1;
                int maximum = ic;
                int clientRange = maximum - minimum + 1;
                int keysRange = keys.size();
                int valsRange = keys.size();
//                int randomNum =  rn.nextInt(range) + minimum;
                long startTime = System.nanoTime();
                for(int ki = 0 ; ki < 30 ; ki++){
                    for (int vi = 0 ; vi < 30 ; vi++){
                        try{
                            clients.get(rn.nextInt(clientRange) + minimum)
                                    .put(keys.get(rn.nextInt(keysRange) + minimum),
                                            vals.get(rn.nextInt(valsRange) + minimum), false, true);
                        } catch (Exception e) {
                            System.out.println("Cannot execute put ");
                        }
                    }
                }
                long endTime = System.nanoTime();
                System.out.println("Total run time for " + 30*30 + "puts: " +
                        ((float)(endTime-startTime) / 1000000000));
                System.out.println("Throughput for " + 100*100 + "puts: " +
                        1/(((float)(endTime-startTime) / 1000000000)/(100*100)));
            }
        }

    }


    @Test
    public void testAddNodeTime(){
        long startTime = System.nanoTime();
        int nodes = 10;
        for (int i =0; i < nodes ; i++){
            ecs1.addNode("FIFO", 123);
        }
        long endTime = System.nanoTime();
        System.out.println("Total run time to add " + nodes + " nodes: " + ((float)(endTime-startTime) / 1000000000));
    }


    @Test
    public void testRemoveNodeTime(){
        int nodes = 10;
        for (int i =0; i < nodes ; i++){
            ecs2.addNode("FIFO", 123);
        }
        TreeMap<String, IECSNode> metadata = ecs2.nodes;
        LinkedList<String> removedNodes = new LinkedList<String>();
        for (Map.Entry<String,IECSNode> md : metadata.entrySet()){
            removedNodes.add(md.getValue().getNodeName());
        }
        long startTime = System.nanoTime();
        ecs2.removeNodes(removedNodes);
        long endTime = System.nanoTime();
        System.out.println("Total run time to remove " + removedNodes.size() +" nodes: " +
                ((float)(endTime-startTime) / 1000000000));
    }
}
