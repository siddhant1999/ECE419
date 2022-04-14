package testing;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import app_kvServer.KVServer;
import app_kvServer.KeyValue;
import client.KVStore;
import com.sun.source.tree.Tree;
import ecs.ECSNode;
import ecs.IECSNode;
import shared.HashFunc;

public class TestUtility {

    public static void putXgetY(KVStore client, double decPUT, int totalRange, String cacheType) throws Exception{
        int numKeys = (int) (totalRange*decPUT);
        Random ran = new Random(1);
        long startTime = System.nanoTime();
        int percentPUT = (int) (decPUT*100);
        for ( int i = 0; i < numKeys ; i++) {
            client.put("key_"+Integer.toString(i), "val_"+Integer.toString(i), true, false);
        }
        for (int i = 0 ; i < totalRange-numKeys ; i++) {
            int rand = ran.nextInt(numKeys);
            client.get("key_"+Integer.toString(rand));
        }
        long endTime = System.nanoTime();
        System.out.println(cacheType + " Total Run Time of " + percentPUT + "% put, " + Integer.toString(100-percentPUT) +
                "% get: " + ((float)(endTime-startTime) / 1000000000) + "s");
    }

    public static LinkedList<KVStore> startNClient(int n) {
        LinkedList<KVStore> clients = new LinkedList<>();
        for (int i = 0 ; i<n; i++){
            clients.add(new KVStore("127.0.0.1", i+20000));
        }
        return clients;
    }

    public static LinkedList<KVServer> startNServers(int n, int cacheSize, String strat, boolean connectECS) {
        LinkedList<KVServer> server = new LinkedList<>();
        for (int i = 0 ; i<n; i++){
            server.add(startServer(i+20000, cacheSize, strat, connectECS));
        }
        return server;
    }

    public static LinkedList<String> getKeys(){
        LinkedList<String> keys = new LinkedList<>();
        String folderPath = "maildir";
        File dir = new File(folderPath);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                //iterate over files in dir
                for(File file2 : file.listFiles()){
                    try {
                        BufferedReader br
                                = new BufferedReader(new FileReader(file2));
                        br.readLine();
                        keys.add(br.readLine());
                    } catch (Exception e) {
                        System.out.println("File not found");
                    }

                }
            }
        }
        return keys;
    }
    public static LinkedList<String> getValues(){
        LinkedList<String> vals = new LinkedList<>();
        String folderPath = "maildir";
        File dir = new File(folderPath);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                //iterate over files in dir
                for(File file2 : file.listFiles()){
                    try {
                        BufferedReader br
                                = new BufferedReader(new FileReader(file2));
                        vals.add(br.readLine());
                    } catch (Exception e) {
                        System.out.println("File not found");
                    }

                }
            }
        }
        return vals;
    }

    public static TreeMap<String, IECSNode> generateOwnMetadata(KVServer kvServer){
        TreeMap<String, IECSNode> metadata = new TreeMap<>();
        metadata.put(kvServer.hashedName, new ECSNode(kvServer.getHostname(), kvServer.getPort()));
        return metadata;
    }

    public static boolean checkLLContainsKV(LinkedList<KeyValue> ll, KeyValue kv){
        for (KeyValue llKV : ll) {
            if (llKV.getKey().equals(kv.getKey()) && llKV.getValue().equals(kv.getValue())) return true;
        }
        return false;
    }

    public static void printLLKV(LinkedList<KeyValue> ll, String name) {
        System.out.print(name + ": ");
        for(KeyValue kv : ll){
            System.out.print("("+kv.getKey()+","+kv.getValue()+"),");
        }
        System.out.print("\n");
    }

    public static LinkedList<KeyValue> getPresentKeys(KVServer server, LinkedList<String> keys) {
        LinkedList<KeyValue> kvPairs = new LinkedList<KeyValue>();
        for (int i = 0; i < keys.size(); i++) {
            try {
                String res = server.getKV(keys.get(i));
                kvPairs.add(new KeyValue(keys.get(i), res));
            } catch (Exception e) {}
        }
        return kvPairs;
    }

    public static KVServer startServer(int port, int cacheSize, String strat, boolean connectECS) {
        KVServer s = new KVServer(port, cacheSize, strat, connectECS);
        //need to update metadata
        s.initKVServer(TestUtility.generateOwnMetadata(s));
        s.startServer();
        s.clearStorage();
        s.start();
        return s;
    }

    public static void printAllData(TreeMap<String, TreeMap<String,
            LinkedList<KeyValue>>> allData, TreeMap<String, IECSNode> serversMetaData,
                                    String funcName){
        System.out.println("Printing All Data from " + funcName);
        System.out.println("Number of Servers: " + allData.size());

        for (Map.Entry<String, TreeMap<String, LinkedList<KeyValue>>> entry : allData.entrySet()) {
            System.out.println("=====Server " + serversMetaData.get(entry.getKey()).getNodePort()+"=====");
            String serverKey = entry.getKey();
            //first print first key
            TestUtility.printLLKV(entry.getValue().get(serverKey),
                    Integer.toString(serversMetaData.get(serverKey).getNodePort()));
            //print prev
            String prev = HashFunc.findPrev(serverKey, serversMetaData);
            if (!prev.equals(serverKey)) {
                TestUtility.printLLKV(entry.getValue().get(prev),
                        Integer.toString(serversMetaData.get(prev).getNodePort()));
            }
            //print prev prev
            String pp = HashFunc.findPrev(prev, serversMetaData);
            if (!pp.equals(serverKey)) {
                TestUtility.printLLKV(entry.getValue().get(pp),
                        Integer.toString(serversMetaData.get(pp).getNodePort()));
            }
            if(entry.getValue().containsKey("other")){
                TestUtility.printLLKV(entry.getValue().get("other"), "other");
            }


        }
        System.out.println("Finished Printing All Data");

    }

    public static void printPortFromHashName(String hashName, TreeMap<String, IECSNode> serversMetaData) {
        System.out.print(serversMetaData.get(hashName).getNodePort() + ": ");
    }

    public static boolean inAllDataLL(TreeMap<String, TreeMap<String,
            LinkedList<KeyValue>>> allData, String hostServer, String replicatedServer, KeyValue kv){
        try{
            return checkLLContainsKV(allData.get(hostServer).get(replicatedServer), kv);
        } catch (Exception e){
            System.out.println("Unable to determine if value in all data LL");
            return false;
        }
    }
}
