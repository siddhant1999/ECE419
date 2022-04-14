package app_kvClient;

import client.KVCommInterface;
import client.KVStore;
import logger.LogSetup;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import shared.messages.KVMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

public class KVClient implements IKVClient {
    private static Logger logger = Logger.getRootLogger();
    private BufferedReader stdin;
    private static final String PROMPT = "kvClient> ";
    private boolean stop = false;
    private KVStore client = null;

    private String serverAddress;
    private int serverPort;
    public enum SocketStatus{CONNECTED, DISCONNECTED, CONNECTION_LOST};

    @Override
    public void newConnection(String hostname, int port) throws Exception{
        // TODO Auto-generated method stub
        client = new KVStore(hostname,port);
        client.connect();
        logger.info("Connection established");
    }

    @Override
    public KVCommInterface getStore(){
        return client;
    }

    public void run(){
        while(!stop) {
            stdin = new BufferedReader(new InputStreamReader(System.in));
            System.out.print(PROMPT);

            try {
                String cmdLine = stdin.readLine();
                this.handleCommand(cmdLine);
            } catch (IOException e) {
                stop = true;
                printError("Command Line Interface Not Responding, Application Terminated");
            }
        }
    }

    private void handleCommand(String cmdLine) {
        String[] tokens = cmdLine.split("\\s+");

        if(tokens[0].equals("quit")) {
            stop = true; //exits out of the command line
            disconnect();
            System.out.println(PROMPT + "Application Exit");
        } else if (tokens[0].equals("help")) {
            printHelp();
        } else if (tokens[0].equals("logLevel")) {
            if(tokens.length == 2) {
                String level = setLevel(tokens[1]);
                if(level.equals(LogSetup.UNKNOWN_LEVEL)) {
                    printError("No valid log level!");
                    printPossibleLogLevels();
                } else {
                    System.out.println(PROMPT +
                            "Log level changed to level " + level);
                }
            } else {
                printError("Invalid number of parameters!");
            }
        } else if (tokens[0].equals("get")) {
            if(tokens.length == 2){
                if (client != null) { //may also need to check client.isRunning() if implemented
                    get(tokens[1]);
                } else {
                    printError("Not Connected!");
                }
            } else if (tokens.length == 1){
                printError("No key passed");
            } else {
                printError("More than 1 key passed");
            }
        } else if (tokens[0].equals("put")) {
            if(tokens.length == 3) {
                if (client!=null) {
                    String k = tokens[1];
                    String v = tokens[2];
                    put(k,v);
                } else {
                    printError("Not Connected!");
                }
            } else {
                printError("Key and Value arguments not provided");
            }
        } else if (tokens[0].equals("disconnect")) {
            if (tokens.length == 1) {
                disconnect();
            } else {
                printError("Incorrect number of arguments");
            }
        } else if (tokens[0].equals("connect")) {
            if(tokens.length == 3) {
                try {
                    serverAddress = tokens[1];
                    serverPort = Integer.parseInt(tokens[2]);
                    newConnection(serverAddress, serverPort);
                    handleStatus(SocketStatus.CONNECTED);
                } catch(NumberFormatException nfe) {
                    printError("No valid address. Port must be a number!");
                    logger.error("Unable to parse argument <port>", nfe);
                } catch (UnknownHostException e) {
                    printError("Unknown Host!");
                    logger.error("Unknown Host!", e);
                } catch (IOException e) {
                    printError("Could not establish connection!");
                    logger.error("Could not establish connection!", e);
                } catch (Exception e) {
                    printError("Error Occurred with KVStore");
                    logger.error("Error Occurred with KVStore!", e);
                }
            } else {
                printError("Please provide address and port value for connect command");
            }
        } else if (tokens[0].equals("metadata")) {
            try{
                if (client!=null) {
                    getMetadata();
                } else {
                    printError("Not Connected!");
                }
            } catch (Exception e) {
                System.out.println("Error in getting Metadata");
                e.printStackTrace();
            }
        } else if (tokens[0].equals("clientMetadata")){
            getKVStoreMetadata();
        } else if (tokens[0].equals("a")){
            getAllData();
        } else if (tokens[0].equals("deleteAll")){
            deleteAllData();
        } else {
            printError("Unknown Command");
            printHelp();
        }
    }

    private void put(String key, String value) {
        if (key.getBytes().length <= 20 && value.getBytes().length <= 120*1000) {
            try {
                logger.info("PUT key:\t '" + key + "'" +  " value:\t '" + value + "'");
                KVMessage kvMsg = client.put(key, value, false, true);
                handleNewKVMessage(kvMsg);
            } catch(Exception e){
                logger.info("ERROR PUT key:\t '" + key + "' Error Seen: " + e.getMessage());
                disconnect();
                handleStatus(SocketStatus.CONNECTION_LOST);
            }
        } else if (key.getBytes().length > 20) {
            printError("Key Size larger than 20 Bytes");
        } else if (value.getBytes().length > 120*1000) {
            printError("Value Size larger than 120 kBytes");
        }
    }

    private void get(String key) {
        if (key.getBytes().length <= 20) {
            try {
                logger.info("GET key:\t '" + key + "'");
                KVMessage kvMsg = client.get(key);
                handleNewKVMessage(kvMsg);
            } catch (Exception e) {
                logger.info("ERROR GET key:\t '" + key + "' Error Seen: " + e.getMessage());
                disconnect();
                handleStatus(SocketStatus.CONNECTION_LOST);
            }
        } else {
            printError("Key Size larger than 20 Bytes");
        }
    }

    private void getMetadata(){
        try {
            client.getMetadata();
        } catch (Exception e) {
            logger.info("ERROR GETMETDATA: " + e.getMessage());
            disconnect();
            handleStatus(SocketStatus.CONNECTION_LOST);
        }
    }

    private void getKVStoreMetadata(){
        if (client == null) {
            System.out.println("Not connected!");
            return;
        }
        client.printMetadata("KV Client");
    }

    private void getAllData(){
        if (client == null) {
            System.out.println("Not connected!");
            return;
        }
        client.getAllData();
    }

    public void deleteAllData(){
        if (client == null) {
            System.out.println("Not connected!");
            return;
        }
        client.deleteAllData();
    }

    private void disconnect(){
        if (client != null) {
            serverPort = client.port;
            serverAddress = client.address;
            client.disconnect();
            client = null;
            handleStatus(SocketStatus.DISCONNECTED);
        } else {
            System.out.println("Not connected!");
        }
    }

    public void handleStatus(SocketStatus status){
        if(status == SocketStatus.CONNECTED) {
            System.out.println(PROMPT +"Connection Started: "
                    + serverPort + " / " + serverAddress);
        } else if (status == SocketStatus.DISCONNECTED) {
            System.out.println(PROMPT + "Connection terminated: "
                    + serverPort + " / " + serverAddress);
        } else if (status == SocketStatus.CONNECTION_LOST) {
            System.out.println("Connection lost: "
                    + serverPort + " / " + serverAddress);
        }
    }

    public void handleNewKVMessage(KVMessage kvMessage) {
        if(!stop) {
            if (kvMessage != null) {
                switch (kvMessage.getStatus()) {
                    case GET_SUCCESS:
                        logger.info("GET Success. Returned: " + kvMessage.getValue());
                        System.out.println("GET Success. Returned: " + kvMessage.getValue());
                        break;
                    case GET_ERROR:
                        logger.error("GET Error: Not Successful");
                        System.out.println("GET Error");
                        break;
                    case PUT_SUCCESS:
                        logger.info("PUT New Success.");
                        System.out.println("PUT Success!");
                        break;
                    case PUT_UPDATE:
                        logger.info("PUT Update Success.");
                        System.out.println("PUT Update Success!");
                        break;
                    case PUT_ERROR:
                        logger.error("PUT Error: Not Successful");
                        System.out.println("PUT Error");
                        break;
                    case DELETE_SUCCESS:
                        logger.info("DELETE Success");
                        System.out.println("PUT Delete Success!");
                        break;
                    case DELETE_ERROR:
                        logger.error("DELETE Error: Not Successful");
                        System.out.println("PUT DELETE Error");
                        break;
                }
            } else {
                logger.error("Error! Returned Message Empty");
            }
        }
    }

    private String setLevel(String levelString) {

        if(levelString.equals(Level.ALL.toString())) {
            logger.setLevel(Level.ALL);
            return Level.ALL.toString();
        } else if(levelString.equals(Level.DEBUG.toString())) {
            logger.setLevel(Level.DEBUG);
            return Level.DEBUG.toString();
        } else if(levelString.equals(Level.INFO.toString())) {
            logger.setLevel(Level.INFO);
            return Level.INFO.toString();
        } else if(levelString.equals(Level.WARN.toString())) {
            logger.setLevel(Level.WARN);
            return Level.WARN.toString();
        } else if(levelString.equals(Level.ERROR.toString())) {
            logger.setLevel(Level.ERROR);
            return Level.ERROR.toString();
        } else if(levelString.equals(Level.FATAL.toString())) {
            logger.setLevel(Level.FATAL);
            return Level.FATAL.toString();
        } else if(levelString.equals(Level.OFF.toString())) {
            logger.setLevel(Level.OFF);
            return Level.OFF.toString();
        } else {
            return LogSetup.UNKNOWN_LEVEL;
        }
    }

    private void printPossibleLogLevels() {
        System.out.println(PROMPT
                + "Possible log levels are:");
        System.out.println(PROMPT
                + "ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF");
    }

    private void printHelp(){
        StringBuilder sb = new StringBuilder();
        sb.append(PROMPT).append("ECHO CLIENT HELP (Usage):\n");
        sb.append(PROMPT);
        sb.append("::::::::::::::::::::::::::::::::");
        sb.append("::::::::::::::::::::::::::::::::\n");
        sb.append(PROMPT).append("connect <host> <port>");
        sb.append("\t establishes a connection to a server\n");
        sb.append(PROMPT).append("get <key>");
        sb.append("\t\t returns value for key from the server \n");
        sb.append(PROMPT).append("put <key> <value>");
        sb.append("\t\t Inserts value to key or updates key with value in server \n");
        sb.append(PROMPT).append("put <key>");
        sb.append("\t\t Deletes key from the server \n");
        sb.append(PROMPT).append("disconnect");
        sb.append("\t\t\t disconnects from the server \n");

        sb.append(PROMPT).append("logLevel");
        sb.append("\t\t\t changes the logLevel \n");
        sb.append(PROMPT).append("\t\t\t\t ");
        sb.append("ALL | DEBUG | INFO | WARN | ERROR | FATAL | OFF \n");

        sb.append(PROMPT).append("quit ");
        sb.append("\t\t\t exits the program");
        System.out.println(sb.toString());
    }

    private void printError(String e) {System.out.println(PROMPT + "Error: " + e);}

    public static void main(String[] args) {
        try {
            new LogSetup("logs/client.log", Level.OFF);
            KVClient kvclient = new KVClient();
            kvclient.run();
        } catch (IOException e) {
            System.out.println("Error! Cannot initialize logger");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
