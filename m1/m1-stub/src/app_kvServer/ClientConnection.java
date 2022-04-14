package app_kvServer;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import java.io.InputStream;
import java.io.InputStreamReader;


import java.io.BufferedReader;

import shared.Serializer;
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
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 128 * BUFFER_SIZE;
	
	private Socket clientSocket;
	private InputStream input;
	private OutputStream output;
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
			output = clientSocket.getOutputStream();
			input = clientSocket.getInputStream();
			
			while(isOpen) {
				try {
					KVMessage KVResponse = handleInput();
					handleOutput(KVResponse);
				} catch (IOException ioe) {
					logger.error("Error! Connection lost!");
					isOpen = false;
				} catch (NullPointerException npe) {
					/*
					 in handleInput, if connection is suddenly terminated, NullPointerException
					 errors also occur
					 */
					logger.error("Error! Connection lost!");
					isOpen = false;
				} catch (Exception e) {
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


	private KVMessage handleInput() throws IOException, NullPointerException {
		KVMessage kvMessage = Serializer.deserialize(new BufferedReader(new InputStreamReader(input)));
		KVMessage returnMessage = null;
		if(kvMessage.getStatus().equals(StatusType.GET)){
			try {
				logger.info("RECEIVED GET: " + kvMessage.getKey());
				String returnValue = kvServer.getKV(kvMessage.getKey());
				returnMessage = new KVMessageImplementation(kvMessage.getKey(), returnValue, StatusType.GET_SUCCESS);
			} catch (Exception e) {
				logger.error("ERROR GET: "+kvMessage.getKey());
				returnMessage = new KVMessageImplementation(kvMessage.getKey(), "", StatusType.GET_ERROR);
			}
		} else if (kvMessage.getStatus().equals(StatusType.PUT)) {
			if (kvMessage.getValue().equals("null")){
				try {
					if(!kvServer.inCache(kvMessage.getKey()) && !kvServer.inStorage(kvMessage.getKey())){
						throw new Exception("PUT DELETE key not present");
					}
					logger.info("RECEIVED DELETE: " + kvMessage.getKey());
					kvServer.putKV(kvMessage.getKey(),kvMessage.getValue());
					returnMessage = new KVMessageImplementation(kvMessage.getKey(),kvMessage.getValue(), StatusType.DELETE_SUCCESS);
				} catch (Exception e) {
					logger.error("ERROR DELETE: " + kvMessage.getKey());
					returnMessage = new KVMessageImplementation(kvMessage.getKey(),kvMessage.getValue(),StatusType.DELETE_ERROR);
				}
			} else if (kvServer.inCache(kvMessage.getKey()) || kvServer.inStorage(kvMessage.getKey())) {
				try {
					logger.info("RECEIVED PUT UPDATE: " + kvMessage.getKey());
					kvServer.putKV(kvMessage.getKey(),kvMessage.getValue());
					returnMessage = new KVMessageImplementation(kvMessage.getKey(),kvMessage.getValue(), StatusType.PUT_UPDATE);
				} catch (Exception e) {
					logger.error("ERROR PUT UPDATE: (" + kvMessage.getKey() + "," + kvMessage.getValue()+")");
					returnMessage = new KVMessageImplementation(kvMessage.getKey(),kvMessage.getValue(),StatusType.PUT_ERROR);
				}
			} else {
				try {
					logger.info("RECEIVED PUT: (" + kvMessage.getKey()+ "," + kvMessage.getValue()+")");
					kvServer.putKV(kvMessage.getKey(),kvMessage.getValue());
					returnMessage = new KVMessageImplementation(kvMessage.getKey(),kvMessage.getValue(), StatusType.PUT_SUCCESS);
				} catch (Exception e) {
					logger.error("ERROR PUT: (" + kvMessage.getKey()+ "," + kvMessage.getValue()+")");
					returnMessage = new KVMessageImplementation(kvMessage.getKey(),kvMessage.getValue(),StatusType.PUT_ERROR);
				}
			}
		}
		return returnMessage;
	}

	public void handleOutput(KVMessage kvMessage) throws IOException {
		byte[] msgBytes = Serializer.serialize(kvMessage);
		output.write(msgBytes, 0, msgBytes.length);
		output.flush();
		logger.info("SENDING (k,v,status): (" +
				kvMessage.getKey() + ", " + kvMessage.getValue() + ", " + kvMessage.getStatus()+")");
	}

}
