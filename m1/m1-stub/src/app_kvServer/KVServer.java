package app_kvServer;
import java.net.InetAddress;
import java.io.IOException;
import java.lang.Exception;

import logger.LogSetup;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.net.BindException;
import java.lang.IllegalArgumentException;

// import logging.LogSetup;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import app_kvServer.cache.FIFOCache;
import app_kvServer.cache.LFUCache;
import app_kvServer.cache.Cache;
import app_kvServer.cache.LRUCache;
import app_kvServer.cache.CacheImplement;

public class KVServer extends Thread implements IKVServer {
	/**
	 * Start KV Server at given port
	 * @param port given port for storage server to operate
	 * @param cacheSize specifies how many key-value pairs the server is allowed
	 *           to keep in-memory
	 * @param strategy specifies the cache replacement strategy in case the cache
	 *           is full and there is a GET- or PUT-request on a key that is
	 *           currently not contained in the cache. Options are "FIFO", "LRU",
	 *           and "LFU".
	 */

	private int port;
	private int cacheSize;
	private CacheStrategy strategy;

    private ServerSocket serverSocket;
    private boolean running;
	private CacheImplement cache;

	private static Logger logger = Logger.getRootLogger();

	public KVServer(int port, int cacheSize, String strat) throws Exception{
		this.port = port;
		this.cacheSize = cacheSize;
		this.cache = new CacheImplement(strat, cacheSize);
		switch(strat){
			case("FIFO"):
				this.strategy = CacheStrategy.FIFO;
			case("LRU"):
				this.strategy = CacheStrategy.LRU;
			case("LFU"):
				this.strategy = CacheStrategy.LFU;
			default:
				this.strategy = CacheStrategy.None;

		}
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
					new KVServer(port, cacheSize, strategy).start();
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
			if (this.cache.inCache(key)) {
				return true;
			} else {
				String fname = this.cache.onDisk(key);
	//			System.out.println("fname retrieved: " + fname);
				if (fname!=null && fname!="null"){
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	@Override
    public boolean inCache(String key){
		return this.cache.inCache(key);
	}

	@Override
    public String getKV(String key) throws Exception{
		String val = this.cache.getValue(key);
		if (val == "null" || val == null) {
			throw new Exception("Key not found in Cache or Disk");
		}
		return val;
	}

	@Override
    public void putKV(String key, String value) throws Exception{
		KeyValue KV = new KeyValue(key, value);
		if(!this.cache.putKV(KV)){
			throw new Exception("Put operation into KV not successful");
		}
	}

	@Override
    public void clearCache(){
		this.cache.clearCache();
	}

	@Override
    public void clearStorage(){
		// TODO Auto-generated method stub
	}

	@Override
    public void run(){
		running = initializeServer();
		// System.out.println("running" + running + " " + serverSocket);
        
        if(serverSocket != null) {
	        while(running){
	            try {
					
					Socket client = serverSocket.accept(); // waits for connection on port
					
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
			serverSocket.close();
		} catch (IOException e) {
			logger.error("Error! " +
					"Unable to close socket on port: " + port, e);
		}
	}

	@Override
    public void close(){
		kill();

		// TODO Auto-generated method stub
	}
}