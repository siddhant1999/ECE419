package app_kvECS;

import ecs.IECSNode;
import shared.SocketLib;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Map;
import java.util.TreeMap;

public class CrashDetection extends Thread{
    ECSClient ecsClient;
    Socket socket;
    public CrashDetection(ECSClient ecsClient){
        this.ecsClient = ecsClient;
        socket = new Socket();
    }

    public void run(){
        while(ecsClient != null){
            //checks for ECS Client Crashes
            try{
                Thread.sleep(5000);
                TreeMap<String, IECSNode> ecsClientNodes = new TreeMap<>(ecsClient.nodes);
                if (!ecsClient.blockCrashDetection){
                    for (Map.Entry<String, IECSNode> entry : ecsClientNodes.entrySet()) {
                        //check if socket is alive
                        int port = entry.getValue().getNodePort();
                        String host = entry.getValue().getNodeHost();
                        try{
                            socket = new Socket(host,port);
                            OutputStream os = socket.getOutputStream();
                            InputStream is = socket.getInputStream();
                            os.close();
                            is.close();
                            socket.close();

                        } catch (IOException ioe) {
                            System.out.println("Socket " + port + "is dead... handling crash");
                            ecsClient.handleServerCrash(entry.getKey());
                            System.out.print(ecsClient.PROMPT);
                        }
                    }
                }

            } catch (Exception e){
                System.out.println("Error in Crash Detection has Occurred");
                e.printStackTrace();
            }
        }
    }
}
