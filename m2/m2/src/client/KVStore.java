package client;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

import app_kvClient.KVClient;
import app_kvServer.KeyValue;
import ecs.IECSNode;
import shared.HashFunc;
import shared.SocketLib;
import shared.messages.KVMessage;
import shared.messages.KVMessageImplementation;

import java.util.*;

import org.apache.log4j.Logger;
import testing.TestUtility;

public class KVStore implements KVCommInterface {
	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	public int port;
	public String address;
	boolean running;
	
	private ObjectOutputStream output;
 	private ObjectInputStream input;

	private Socket clientSocket;
	public Logger logger = Logger.getRootLogger();
	public TreeMap<String,IECSNode> serversMetaData;

	public KVStore(String address, int port) {
		this.port = port;
		this.address = address;
		this.running = true;
	}

	@Override
	public void connect() throws Exception {
		clientSocket = new Socket(address, port);
		try {
			output = new ObjectOutputStream(clientSocket.getOutputStream());
			input = new ObjectInputStream(clientSocket.getInputStream());
			running = true;
			logger.info("Connected to " + address + ":" + port);
		} catch (Exception e) {
			logger.error("Connection could not be established!");
		}

	}

	private void tearDownConnection() throws IOException {
		running = false;
		logger.info("tearing down the connection to " + port);
		if (clientSocket != null) {
			input.close();
			output.close();
			clientSocket.close();
			clientSocket = null;
			logger.info("connection closed!");
		}
	}

	@Override
	public void disconnect() {
		logger.info("try to disconnect connection to " + port);

		try {
			tearDownConnection();
		} catch (IOException ioe) {
			logger.error("Unable to close connection!");
		}
	}

	public KVMessage receiveMessage() throws IOException, ClassNotFoundException {
		KVMessage kvMessage = null;
		try{
			kvMessage = (KVMessage) input.readObject();
		} catch (EOFException e){}
 		if (kvMessage == null){
//			System.out.println("Recieved kvMessage is null");
//			return null;
			 System.out.println("Connection is lost from " + port + ", disconnecting");
			throw new IOException("Connection Closed");
		}
		logger.info(port+ ": RECEIVED (k,v,status): (" +kvMessage.messageID()+")");
		return kvMessage;
	}

	public void printMetadata(String functionCalled){
		try {
			System.out.println("Begin Printing Metadata from " + functionCalled + "----------");
			if (serversMetaData != null){
				for (Map.Entry<String, IECSNode> entry : new TreeMap<String, IECSNode>(serversMetaData).entrySet()) {
					System.out.println("Node: " + entry.getKey()+ " " + entry.getValue().getNodePort());
				}
			} else {
				System.out.println("Metadata is null");
			}
			System.out.println("Finish Printing Metadata---------");

		} catch (Exception e ){
			System.out.println("printMetadata exception: " + e);
			e.printStackTrace();
		}
	}

	@Override
	public KVMessage put(String key, String value, boolean forcePut, boolean replicate) throws Exception {
		KVMessage sentMessage = new KVMessageImplementation(key, value, KVMessage.StatusType.PUT, forcePut,replicate, true);
		sendMessage(sentMessage);
		return receiveCorrectMessage(sentMessage, receiveMessage());
	}

	/**
	 * Used for server calls
	 * @param key
	 *            the key that identifies the given value.
	 * @param value
	 *            the value that is indexed by the given key.
	 * @param forcePut
	 * @param replicate
	 * @param fromClient
	 * @return
	 * @throws Exception
	 */
	@Override
	public KVMessage put(String key, String value, boolean forcePut, boolean replicate, boolean fromClient) throws Exception {
		KVMessage sentMessage = new KVMessageImplementation(key, value, KVMessage.StatusType.PUT, forcePut,replicate, fromClient);
		sendMessage(sentMessage);
		return receiveCorrectMessage(sentMessage, receiveMessage());
	}

	@Override
	public KVMessage get(String key) throws Exception {
		KVMessage sentMessage = new KVMessageImplementation(key, null, KVMessage.StatusType.GET);
		sendMessage(sentMessage);
		return receiveCorrectMessage(sentMessage, receiveMessage());
	}
	public void sendMessage(KVMessage kvMessage) throws IOException {
		logger.info(port + ": SENDING (k,v,status): (" +
				kvMessage.getKey() + ", " + kvMessage.getValue() + ", " + kvMessage.getStatus()+")");
		System.out.println("Sending Message: ");
		kvMessage.printKVMessage();
		System.out.println("");
		output.writeObject(kvMessage);
		output.flush();
	}

	private KVMessage receiveCorrectMessage(KVMessage sentMessage, KVMessage receiveMessage) throws Exception{
		System.out.println("Received Message: ");

		receiveMessage.printKVMessage();
		serversMetaData = receiveMessage.getMetadata();

		while (receiveMessage.getStatus() == KVMessage.StatusType.SERVER_NOT_RESPONSIBLE) {
			/*
				Not sure if need to include other states
				SERVER_WRITE_LOCK
				SERVER_STOPPED
				-> need to determine how to handle that

				This while loop should only loop through once, due to connectResponsibleServer
			 */
			logger.info(port + " not responsible");
			serversMetaData = receiveMessage.getMetadata();
			connectResponsibleServer(sentMessage);
			sendMessage(sentMessage);
			receiveMessage = receiveMessage();
		}
		if (receiveMessage.getStatus() == KVMessage.StatusType.SERVER_STOPPED) {
			throw new Exception("Operation failed, SERVER_STOPPED");
		} else if (receiveMessage.getStatus() == KVMessage.StatusType.SERVER_WRITE_LOCK) {
			throw new Exception("Operation Failed, SERVER_WRITE_LOCK");
		}
		System.out.println("Currently Connected Server: " + this.port + "\n");
		return receiveMessage;
	}

	/**
	 * Used for testing only: Prints metadata of currently connected server
	 * @throws Exception
	 */
	public void getMetadata() throws Exception {
		KVMessage sentMessage = new KVMessageImplementation(null, null, KVMessage.StatusType.GET_METADATA);
		sendMessage(sentMessage);
		KVMessage returnMessage = receiveMessage();
		serversMetaData = new TreeMap<>(returnMessage.getMetadata());
		returnMessage.printMetadata();
	}

	private void connectResponsibleServer(KVMessage kvMessage) throws Exception{
		//get next largest value for server
		String responsibleServerHash = HashFunc.findNextLargest(HashFunc.hashString(kvMessage.getKey()),
				serversMetaData);
		if (responsibleServerHash == null) {
			throw new Exception("No Servers Running");
		}
		if(clientSocket!=null && clientSocket.isConnected()) disconnect();
		logger.info("Trying to Connect to next responsible server: " +
				serversMetaData.get(responsibleServerHash).getNodePort());
		this.port = serversMetaData.get(responsibleServerHash).getNodePort();
		this.address = serversMetaData.get(responsibleServerHash).getNodeHost();
		connect();
		return;
	}

	public TreeMap<String, LinkedList<KeyValue>> getAllDataHelper(String hashedName, LinkedList<KeyValue> allData){
		String p = HashFunc.findPrev(hashedName, serversMetaData);
		String pp = HashFunc.findPrev(p, serversMetaData);
		LinkedList<KeyValue> pLL = new LinkedList<>();
		LinkedList<KeyValue> ppLL = new LinkedList<>();
		LinkedList<KeyValue> cLL = new LinkedList<>();
		LinkedList<KeyValue> other = new LinkedList<>();
		for (KeyValue kv : allData){
			String hashKey = HashFunc.hashString(kv.getKey());
			if (HashFunc.findNextLargest(hashKey, serversMetaData).equals(hashedName)) cLL.add(kv);
			else if (HashFunc.findNextLargest(hashKey, serversMetaData).equals(p)) pLL.add(kv);
			else if (HashFunc.findNextLargest(hashKey, serversMetaData).equals(pp)) ppLL.add(kv);
			else other.add(kv);
		}
		TreeMap<String, LinkedList<KeyValue>> ret = new TreeMap<String, LinkedList<KeyValue>>();
		ret.put(hashedName, cLL);
		if(!p.equals(hashedName)) ret.put(p, pLL);
		if(!pp.equals(hashedName)) ret.put(pp, ppLL);
		ret.put("other", other);

		return ret;


	}

	public void deleteAllData(){

		if (serversMetaData == null) {
			System.out.println("Metadata is empty try again...");
			return;
		}
		for (Map.Entry<String, IECSNode> entry : serversMetaData.entrySet()) {
			try{
				//connect to the right node

				if(clientSocket!=null && clientSocket.isConnected()) disconnect();
				this.port = entry.getValue().getNodePort();
				this.address = entry.getValue().getNodeHost();
				connect();

				sendMessage(new KVMessageImplementation(null,null, KVMessage.StatusType.DELETE_ALL_DATA));
				receiveMessage();
			} catch (Exception e) {
				System.out.println("Error getting data from " + entry.getValue().getNodePort());
				e.printStackTrace();
			}
		}
//		if(clientSocket!=null && clientSocket.isConnected()) disconnect();

	}


	public TreeMap<String, TreeMap<String, LinkedList<KeyValue>>> getAllData() {
		System.out.println("Getting All Data");
		TreeMap<String, TreeMap<String, LinkedList<KeyValue>>> allData = new TreeMap<>();
		try{
			getMetadata();
			printMetadata("getAllData");
		} catch (Exception e){
			System.out.println("Cannot get metadata");
		}
		if (serversMetaData == null) {
			System.out.println("Metadata is empty try again...");
			return null;
		}
		for (Map.Entry<String, IECSNode> entry : serversMetaData.entrySet()) {
			try{
				//connect to the right node

				if(clientSocket!=null && clientSocket.isConnected()) disconnect();
				this.port = entry.getValue().getNodePort();
				this.address = entry.getValue().getNodeHost();
				connect();

				sendMessage(new KVMessageImplementation(null,null, KVMessage.StatusType.GET_ALL_DATA));
				KVMessage kvMessage = receiveMessage();
				System.out.println(port + " kvMessage status: " + kvMessage.getStatus());
				LinkedList<KeyValue> serverData = kvMessage.getAllData(); //all data from server
				System.out.println("Getting all data from server " + port);
				TestUtility.printLLKV(serverData, "Server " + port);
				allData.put(entry.getKey(), getAllDataHelper(entry.getKey(), serverData));
			} catch (ConnectException e) {
				System.out.println("Connection " + entry.getValue().getNodePort() + " closed");
			} catch (Exception e){
				System.out.println("Error getting data from " + entry.getValue().getNodePort());
				e.printStackTrace();
			}
		}

		//now print all data
		TestUtility.printAllData(allData, serversMetaData, "KVStore");
		if(clientSocket!=null && clientSocket.isConnected()) System.out.println("Currently connected on " + this.port);
		return allData;

	}
}
