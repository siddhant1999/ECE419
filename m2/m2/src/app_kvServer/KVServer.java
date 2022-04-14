package app_kvServer;
import java.io.IOException;
import java.lang.Exception;

import client.KVStore;
import logger.LogSetup;

import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.net.BindException;
import java.lang.IllegalArgumentException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
// import java.util.logging.Logger

import ecs.IECSNode;
import app_kvServer.cache.CacheImplement;
import shared.HashFunc;
import shared.messages.KVMessage;
import testing.TestUtility;


public class KVServer extends Thread implements IKVServer {
	public enum ServerState {
		STOPPED,
		STARTED, 
		SHUT_DOWN, 
		ERROR
	}

	private int port;
	private String host;
	private int cacheSize;
	private CacheStrategy strategy;

	public ServerState state;
	public Boolean writeLock;
	private TreeMap<String, IECSNode> metadata;
	public String name;
	public String hashedName;

    private ServerSocket serverSocket;
    private boolean running;
	private DataManager dataManager;

	private boolean connectECS;

	private static Logger logger = Logger.getRootLogger();

	/**
	 * Start KV Server at given port
	 * @param port given port for storage server to operate
	 * @param cacheSize specifies how many key-value pairs the server is allowed
	 *           to keep in-memory
	 * @param strat specifies the cache replacement strategy in case the cache
	 *           is full and there is a GET- or PUT-request on a key that is
	 *           currently not contained in the cache. Options are "FIFO", "LRU",
	 *           and "LFU".
	 * @param connectECS True = enables ecs connection, false = disables ecs connection
	 *                   Used for testing purposes
	 */
	public KVServer(int port, int cacheSize, String strat, Boolean connectECS) {
		this.port = port;
		this.host = "127.0.0.1";
		this.name = this.host + ":" + this.port;
		this.hashedName = HashFunc.hashString(this.name);
		this.metadata = new TreeMap<String, IECSNode>();
		this.cacheSize = cacheSize;
		this.state = ServerState.STOPPED;
		this.writeLock = false;
		this.dataManager = new DataManager(strat, cacheSize, Integer.toString(this.port));
		this.connectECS = connectECS;
		switch(strat){
			case("FIFO"):
				this.strategy = CacheStrategy.FIFO;
				break;
			case("LRU"):
				this.strategy = CacheStrategy.LRU;
				break;
			case("LFU"):
				this.strategy = CacheStrategy.LFU;
				break;
			default:
				this.strategy = CacheStrategy.None;
				break;

		}
		logger.info("KVServer initialized on port " + port);

	}

	public static void main(String[] args) {
		try {
			new LogSetup("logs/server.log", Level.ALL);
			if (args.length != 3) {
				System.out.println("Error! Invalid number of arguments!");
				System.out.println("Usage: Server <port> <cacheSize> <cacheStrategy: None, LRU, LFU, FIFO>");
			} else {
				int port = Integer.parseInt(args[0]);
				int cacheSize = Integer.parseInt(args[1]);
				String strategy = args[2];

				try {
					new KVServer(port, cacheSize, strategy, true).start();
				} catch (IllegalArgumentException e) {
					System.out.println("Error! Invalid cache strategy!");
					System.out.println("Usage: Server <port> <cacheSize> <cacheStrategy: None, LRU, LFU, FIFO>");
					System.exit(1);
				} catch (Exception e){
					System.out.println("Error occurred!");
					System.exit(1);
				}

			}
		} catch (IOException e) {
			System.out.println("Error! Unable to initialize logger!");
			e.printStackTrace();
			System.exit(1);
		} catch (NumberFormatException nfe) {
			System.out.println("Error! Invalid argument <port>! Not a number!");
			System.out.println("Usage: Server <port>!");
			System.exit(1);
		}
	}
	
	@Override
	public int getPort(){
		return this.port;
	}

	@Override
    public String getHostname(){
		if (serverSocket != null){
			return serverSocket.getInetAddress().getHostName();
		} else {
			return null;
		}
	}

	@Override
    public CacheStrategy getCacheStrategy(){
		return this.strategy;
	}

	@Override
    public int getCacheSize(){
		return this.cacheSize;
	}

	@Override
	public boolean inStorage(String key){
		try {
			return this.dataManager.inDataManager(key);
		} catch (Exception e) {
			logger.error("Error Occurred in inStorage: "+ e.toString());
			return false;
		}
	}

	@Override
	public boolean inCache(String key){
		return this.inStorage(key);
	}

	@Override
	public String getKV(String key) throws Exception{
		String coordinator = HashFunc.findNextLargest(HashFunc.hashString(key),metadata);
		String replica_1 = HashFunc.findNextLargest(coordinator,metadata);
		String replica_2 = HashFunc.findNextLargest(replica_1, metadata);
		if (this.state == ServerState.SHUT_DOWN || this.state == ServerState.STOPPED) {
			throw new Exception("2 Server Lock");
		} else if (coordinator.equals(this.hashedName) ||
				replica_1.equals(this.hashedName) ||
				replica_2.equals(this.hashedName)) {
			String val = this.dataManager.get(key);
			if (val.equals("null") || val == null) throw new Exception("Key not in server");
			return val;
		} else {
			throw new Exception("1 Not in Server Bounds");
		}
	}

	/**
	 *
	 * @param key Non hashed key to write
	 * @param value value to write
	 * @param forcePut true = only from moving data, key guaranteed to exist, skips lock
	 *                  false = everywhere else
	 * @throws Exception
	 */
	@Override
	public void putKV(String key, String value, boolean forcePut, boolean replicate, boolean fromClient) throws Exception{
		String hashedString = HashFunc.hashString(key);
		String responsibleServer = HashFunc.findNextLargest(hashedString,metadata);
		String successor_1 = HashFunc.findNextLargest(responsibleServer,metadata);
		String successor_2 = HashFunc.findNextLargest(successor_1,metadata);

//		System.out.println("--------- PRINT KV CALLED FOR " + port + "------------");
//		System.out.print("responsible: ");
//		TestUtility.printPortFromHashName(responsibleServer,metadata);
//		System.out.print("successor_1: ");
//		TestUtility.printPortFromHashName(successor_1,metadata);
//		System.out.print("successor_2: ");
//		TestUtility.printPortFromHashName(successor_2,metadata);



		if (this.writeLock == true && !forcePut) {
			throw new Exception("0 Server Lock");
		} else if (!forcePut && (this.state == ServerState.SHUT_DOWN || this.state == ServerState.STOPPED)) {
			throw new Exception("2 Server Shut Down or Stopped");
		} else if (forcePut ||
				(responsibleServer.equals(this.hashedName)) ||
				(!fromClient && (successor_1.equals(this.hashedName) || successor_2.equals(this.hashedName)))){
			this.dataManager.put(key,value);
			if(replicate){
				//TODO: Look to deprecate replicate and forceput with replacement of responsible server

//			if(responsibleServer.equals(this.hashedName)) { //used for testing
				//should always be force putting replicas on successors
				if (!successor_1.equals(hashedName)) replicaPut(key, value, true, successor_1);
				if (!successor_2.equals(hashedName)) replicaPut(key, value, true, successor_2);
			}
		} else {
			throw new Exception("1 Not in Server Bounds");
		}
	}

	@Override
    public void clearCache(){
		this.dataManager.clearDataManager();
	}

	@Override
    public void clearStorage(){
		this.dataManager.clearDataManager();
	}

	@Override
    public void run(){
		running = initializeServer();
		 System.out.println("running" + running + " " + serverSocket);
        
        if(serverSocket != null) {
	        while(running){
	            try {
					logger.info("Listening for client socket connection");
					Socket client = serverSocket.accept(); // waits for connection on port
					logger.info("Client socket connection successful");

					ClientConnection connection =
	                		new ClientConnection(client, this);
	                new Thread(connection).start();
					
	                logger.info("Connected to " 
	                		+ client.getInetAddress().getHostName() 
	                		+  " on port " + client.getPort());
	            } catch (IOException e) {
	            	logger.error("Error! " +
	            			"Unable to establish connection. \n", e);
	            }
	        }
        }
        logger.info("Server stopped.");
	}

	public boolean initializeServer(){
		logger.info("Initializing server socket");
    	try {
            this.serverSocket = new ServerSocket(getPort());
			logger.info("Server listening on port: " +
				serverSocket.getLocalPort());    
			if(this.connectECS) connectECS();
            return true;
        
        } catch (IOException e) {
        	logger.error("Unable to open server socket");
            if(e instanceof BindException) {
            	logger.error("Port already bound: " + port);
            }
            return false;
        }
	}

	@Override
    public void kill(){
		// TODO Auto-generated method stub
		running = false;
		try {
			if (serverSocket!=null){
				serverSocket.close();
			}
		} catch (IOException e) {
			logger.error("Error! " +
					"Unable to close socket on port: " + port, e);
		}
	}

	@Override
    public void close(){
		kill();
	}

	/***********************
	* BEGINNING OF M2 WORK *
	************************/
	
	public void initKVServer(TreeMap<String, IECSNode> ecsMetadata){
		logger.warn("IN init KVServer");
		setMetaData(ecsMetadata);
		this.state = ServerState.STOPPED;
		logger.warn("OUT init KVServer");

	}

	public void startServer() {
		logger.info("KVServer started");
		this.state = ServerState.STARTED;
	}

	public void stopServer() {
		logger.info("KVServer stopped");
		this.state = ServerState.STOPPED;
	}

	public void shutDown() {
		logger.info("KVServer shutDown");
		this.state = ServerState.SHUT_DOWN;
		close();
	}

	public void writeLock() {
		this.writeLock = true;
	}

	public void unLockWrite() {
		this.writeLock = false;
	}

	/**
	 * Used for testing
	 * @param ecsMetaData
	 */
	public void printTreeMap(TreeMap<String, IECSNode> ecsMetaData){
		for (Map.Entry<String, IECSNode> entry : ecsMetaData.entrySet()) {
			IECSNode node = entry.getValue();
			System.out.println("Node: " + node.getNodeName() + " | HashedName: " + node.getHashedName());
		}
	}

	public TreeMap<String, IECSNode> setMetaData(TreeMap<String, IECSNode> ecsMetaData) {
		try {
			writeLock();
			this.metadata = new TreeMap<String, IECSNode>(ecsMetaData);
			unLockWrite();
			return this.metadata;
		} catch (Exception e) {
			unLockWrite();
			logger.error("Error In Updating Server " + this.getHostname() + ":" + this.getPort());
		}
		logger.info("Successfully Updates Server " + this.getHostname() + ":" + this.getPort());
		return null;
	}

	public void connectECS() {
		try {
			logger.info("Waiting for ECS on " + this.getHostname() + ":" + serverSocket.getLocalPort());
			Socket ecs = serverSocket.accept();
			ECSConnection ecsConnection = new ECSConnection(ecs, this);
			new Thread(ecsConnection).start();
			logger.info("Bound to ECS on " + ecs.getInetAddress() + ":" + ecs.getPort());
		} catch (Exception e) {
			logger.error("Error! Unable to establish connection to ECS: ", e);
		}
	}

	public void replicaPut(String key, String value, boolean forcePut, String dstServerHashName) throws Exception{
		//simply create kvstore and send put to the dstServerHashedName
		try{
			IECSNode dstServerNode = metadata.get(dstServerHashName); //error out
			KVStore kvStore = new KVStore(dstServerNode.getNodeHost(), dstServerNode.getNodePort());
			kvStore.connect();
			//do not replicate put, since that results in infinite loop
			kvStore.put(key,value,forcePut, false, false);
			kvStore.disconnect();
		} catch (Exception e) {
			Integer replicaPort = metadata.get(dstServerHashName).getNodePort();
			System.out.println("Unable to update replica at port " + replicaPort);
			e.printStackTrace();
			throw new Exception("Error Updating Replica " + replicaPort + ": " + e.getMessage());
		}

	}

	/**
	 * @param dstServerHashName the hash name of the server in which keys should be moved to
	 *                   ALL KEYS LESS THAN THE SERVERHASH IS MOVED
	 * @param hashUpperBound next upper bound of data that should be moved
	 * @param delete true = all moved data is delete from current server
	 *               false = all moved data is kept
	 * @param replicate true = replicate moved data to other servers; do so for actual run
	 *                  false = do not replicate; used for unit testing moveData
	 * @throws Exception
	 * Assumptions:
	 *  - Metadata is correct; dstServerHashName can be found in metadata
	 *  - On Add: Metadata updated before moveData
	 *  - On Delete: Metadata updated after moveData
	 *
	 * 	- Server bounds is circumvented through (force) boolean var which forces adds despite out of boundary
	 *
	 * m3 Additions:
	 * -> boolean field to update replicas
	 * 	-> if yes, also need to put data and remove data from replicas
	 */
	public void moveData(String dstServerHashName, String hashUpperBound, boolean delete, boolean replicate) throws Exception{
		if(dstServerHashName.equals(hashedName)) return;
		this.writeLock();
		//first initialize KVStore and connect to dstServer
		IECSNode dstServerNode = metadata.get(dstServerHashName); //error out
		KVStore kvStore = new KVStore(dstServerNode.getNodeHost(), dstServerNode.getNodePort());
		kvStore.connect();

		//iterate over all data and move relevant entries to KVServer
		LinkedList<KeyValue> serverData = dataManager.getAllDiskData();
		for (KeyValue kv : serverData) {
			String key = kv.getKey(), val = kv.getValue();
			if (!HashFunc.findNextLargest(HashFunc.hashString(key),metadata).equals(hashUpperBound)) {
				continue;
			}
			if(delete){
				//first deletes it from all replicates (if relevant)
				putKV(key, "null", true, replicate, false);
			}
			//then move it to dst server, which adds it to that server's replicates
			kvStore.put(key, val, true, replicate, false);
		}
		//disconnect from dstServer
		kvStore.disconnect();

		this.unLockWrite();
		logger.info("Successfully Moved Ranged Data to " + dstServerHashName);
		return;
	}

	public void deleteData(String hashUpperBound) {
		if (hashUpperBound.equals("")) {
			clearStorage();
			return;
		}
		LinkedList<KeyValue> serverData = dataManager.getAllDiskData();
		for(KeyValue kv : serverData) {
			String key = kv.getKey();
			if (HashFunc.findNextLargest(HashFunc.hashString(key),metadata).equals(hashUpperBound)) {
				try {
					putKV(key, "null", true, false, false);
				} catch (Exception e) {
					System.out.println("Unable to delete key: " + key);
					e.printStackTrace();
				}
			}
		}
	}

	 public void printMetadata() {
		System.out.println("Start Printing Metadata-----------");
		if (this.metadata == null ) System.out.println("Metadata is null");
		else{
			for (Map.Entry<String, IECSNode> e: this.metadata.entrySet() ) {
				System.out.println(e.getKey()+ ": " + e.getValue().getNodePort());
			}
		}
	 	System.out.println("Stop Printing Metadata-----------");
	 }

	public TreeMap<String, IECSNode> getMetadata() {
		return this.metadata;
	}

	public LinkedList<KeyValue> getAllData(){
		return dataManager.getAllDiskData();
//		return (LinkedList<KeyValue>)dataManager.getAllDiskData().clone();
	}
	
}