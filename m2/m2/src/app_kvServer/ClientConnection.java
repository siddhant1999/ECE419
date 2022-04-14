package app_kvServer;

import java.io.*;
import java.net.Socket;

import shared.messages.KVMessage;
import shared.messages.KVMessage.*;
import shared.messages.KVMessageImplementation;

import java.lang.IllegalArgumentException;
import org.apache.log4j.*;

/**
 * Represents a connection end point for a particular client that is 
 * connected to the server. This class is responsible for message reception 
 * and sending. 
 * The class also implements the echo functionality. Thus whenever a message 
 * is received it is going to be echoed back to the client.
 */
public class ClientConnection implements Runnable {

	private static Logger logger = Logger.getRootLogger();
	
	private boolean isOpen;
	
	private Socket clientSocket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private KVServer kvServer;
	/**
	 * Constructs a new CientConnection object for a given TCP socket.
	 * @param clientSocket the Socket object for the client connection.
	 */
	public ClientConnection(Socket clientSocket, KVServer kvServer) {
		this.clientSocket = clientSocket;
		this.isOpen = true;
		this.kvServer = kvServer;
	}
	
	/**
	 * Initializes and starts the client connection. 
	 * Loops until the connection is closed or aborted by the client.
	 */
	public void run() {
		try {
			output = new ObjectOutputStream(clientSocket.getOutputStream());
			input = new ObjectInputStream(clientSocket.getInputStream());
			
			while(isOpen) {
				try {
					KVMessage KVResponse = handleInput();
					handleOutput(KVResponse);
				} catch (IOException ioe) {
					System.out.println("IOException");
					ioe.printStackTrace();
					logger.error("Error! Connection lost! IOException");
					isOpen = false;
				} catch (NullPointerException npe) {
					/*
					 in handleInput, if connection is suddenly terminated, NullPointerException
					 errors also occur
					 */
					logger.error("Error! Connection lost! NullPointerException");
					isOpen = false;
				} catch (Exception e) {
					System.out.println("Error in message ClientConnection");
					logger.error("Error in Sending Message!");
				}
			}
			
		} catch (IOException ioe) {
			logger.error("Error! Connection could not be established!", ioe);
		} catch (NullPointerException npe) {
			logger.error("Error! Message status is none cannot be processed!", npe);
		}
		finally {
			
			try {
				if (clientSocket != null) {
					input.close();
					output.close();
					clientSocket.close();
				}
			} catch (IOException ioe) {
				logger.error("Error! Unable to tear down connection!", ioe);
			}
		}
	}


	private KVMessage handleInput() throws IOException, NullPointerException, ClassNotFoundException {
		KVMessage kvMessage = null;
		try{
			logger.info("Waiting to receive message from client");
			kvMessage = (KVMessage) input.readObject();
		} catch (EOFException e) {
			System.out.println("SEEN EOF");
		}
		logger.info("Received Message from client: " + kvMessage.messageID());
		KVMessage returnMessage = null;
		if(kvMessage.getStatus().equals(StatusType.GET)){
			try {
				logger.info("RECEIVED GET: " + kvMessage.getKey());
				returnMessage = getHandler(kvMessage);
			} catch (Exception e) {
				logger.error(e);
				logger.error("ERROR GET: "+kvMessage.getKey());
				returnMessage = new KVMessageImplementation(kvMessage.getKey(), "", StatusType.GET_ERROR, kvServer.getMetadata());
			}
		} else if (kvMessage.getStatus().equals(StatusType.PUT)) {
			if (kvMessage.getValue().equals("null")){
				try {
					if(!kvServer.inCache(kvMessage.getKey()) && !kvServer.inStorage(kvMessage.getKey())){
						throw new Exception("PUT DELETE key not present");
					}
					logger.info("RECEIVED DELETE: " + kvMessage.getKey());
					returnMessage = putHandler(kvMessage, StatusType.DELETE_SUCCESS);
				} catch (Exception e) {
					logger.error(e);
					logger.error("ERROR DELETE: " + kvMessage.getKey());
					returnMessage = new KVMessageImplementation(kvMessage.getKey(),kvMessage.getValue(),StatusType.DELETE_ERROR, kvServer.getMetadata());
				}
			} else if (kvServer.inCache(kvMessage.getKey()) || kvServer.inStorage(kvMessage.getKey())) {
				try {
					logger.info("RECEIVED PUT UPDATE: " + kvMessage.getKey());
					returnMessage = putHandler(kvMessage, StatusType.PUT_UPDATE);
				} catch (Exception e) {
					logger.error(e);
					logger.error("ERROR PUT UPDATE: (" + kvMessage.getKey() + "," + kvMessage.getValue()+")");
					returnMessage = new KVMessageImplementation(kvMessage.getKey(),kvMessage.getValue(),StatusType.PUT_ERROR, kvServer.getMetadata());
				}
			} else {
				try {
					logger.info("RECEIVED PUT: (" + kvMessage.getKey()+ "," + kvMessage.getValue()+")");
					returnMessage = putHandler(kvMessage, StatusType.PUT_SUCCESS);
				} catch (Exception e) {
					logger.error(e);
					logger.error("ERROR PUT: (" + kvMessage.getKey()+ "," + kvMessage.getValue()+")");
					returnMessage = new KVMessageImplementation(kvMessage.getKey(),kvMessage.getValue(),StatusType.PUT_ERROR, kvServer.getMetadata());
				}
			}
		} else if (kvMessage.getStatus().equals(StatusType.GET_METADATA)) {
			returnMessage = new KVMessageImplementation(null,null,StatusType.GET_METADATA,
					kvServer.getMetadata());
		} else if (kvMessage.getStatus().equals(StatusType.GET_ALL_DATA)) {
			returnMessage = new KVMessageImplementation(StatusType.GET_METADATA,
					kvServer.getAllData());
		} else if (kvMessage.getStatus().equals(StatusType.DELETE_ALL_DATA)) {
			returnMessage = new KVMessageImplementation(null,null,StatusType.DELETE_ALL_DATA);
			kvServer.clearStorage();
			kvServer.clearCache();
		}
		return returnMessage;
	}

	public void handleOutput(KVMessage kvMessage) throws IOException {
		if (kvMessage == null) return;
		logger.info("Sending message to client: " + kvMessage.messageID());

		output.writeObject(kvMessage);
		output.flush();
		logger.info("SENDING (k,v,status): (" +
				kvMessage.getKey() + ", " + kvMessage.getValue() + ", " + kvMessage.getStatus()+")");
	}

	private KVMessage getHandler (KVMessage kvMessage) throws Exception {
		KVMessage returnMessage;
		String returnValue;
		try {
			returnValue = kvServer.getKV(kvMessage.getKey());
			returnMessage = new KVMessageImplementation(kvMessage.getKey(), returnValue, StatusType.GET_SUCCESS, kvServer.getMetadata());
		} catch (Exception e) {
			String ex = e.getMessage();
			if (ex.equals("0 Server Lock")) { // server write locked
				returnMessage = new KVMessageImplementation(kvMessage.getKey(), "null", StatusType.SERVER_WRITE_LOCK, kvServer.getMetadata());
			} else if (ex.equals("1 Not in Server Bounds")) {
				returnMessage = new KVMessageImplementation(kvMessage.getKey(), "null", StatusType.SERVER_NOT_RESPONSIBLE, kvServer.getMetadata());
			} else if (ex.equals("2 Server Lock")) {
				returnMessage = new KVMessageImplementation(kvMessage.getKey(), "null", StatusType.SERVER_STOPPED, kvServer.getMetadata());
			} else {
				throw e;
			}
		}
		return returnMessage;
	}

	private KVMessage putHandler (KVMessage kvMessage, StatusType st ) throws Exception{
		KVMessage returnMessage;
		try {
			logger.info("in put handler putKV with message: " + kvMessage.getKey() + " " + kvMessage.getValue());
			kvServer.putKV(kvMessage.getKey(), kvMessage.getValue(), kvMessage.getForcePut(), kvMessage.getReplicate(), kvMessage.getFromClient());
			returnMessage = new KVMessageImplementation(kvMessage.getKey(), kvMessage.getValue(), st, kvServer.getMetadata());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("putHandler Exception: " + e);
			String ex = e.getMessage();
			if (ex.charAt(0) == "0".charAt(0)) { // server write locked 
				returnMessage = new KVMessageImplementation(kvMessage.getKey(), "null", StatusType.SERVER_WRITE_LOCK, kvServer.getMetadata());
			} else if (ex.charAt(0) == "1".charAt(0)) {
				returnMessage = new KVMessageImplementation(kvMessage.getKey(), "null", StatusType.SERVER_NOT_RESPONSIBLE, kvServer.getMetadata());
			} else if (ex.charAt(0) == "2".charAt(0)) {
				returnMessage = new KVMessageImplementation(kvMessage.getKey(), "null", StatusType.SERVER_STOPPED, kvServer.getMetadata());
			} else {
				throw e;
			}
		}
		return returnMessage;
	}

}
