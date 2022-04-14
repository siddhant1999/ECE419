package app_kvServer;

import java.io.*;
import java.net.Socket;
import java.util.TreeMap;

import ecs.IECSNode;
import shared.messages.KVAdminMessage;
import shared.messages.KVAdminMessage.StatusType;

import org.apache.log4j.Logger;
import app_kvServer.KVServer;


public class ECSConnection implements Runnable {

	private static Logger logger = Logger.getRootLogger();
	
	private boolean isOpen;
	private static final int BUFFER_SIZE = 1024;
	private static final int DROP_SIZE = 128 * BUFFER_SIZE;
	
	private Socket ecsSocket;
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private KVServer kvServer;

	public ECSConnection(Socket ecsSocket, KVServer kvServer){
		this.ecsSocket = ecsSocket;
		this.isOpen = true; 
		this.kvServer = kvServer;
	}

	public void run() {
		try {
			output = new ObjectOutputStream(ecsSocket.getOutputStream());
			input = new ObjectInputStream(ecsSocket.getInputStream());
			
			while(isOpen) {
				try {
					KVAdminMessage KVResponse = handleInput();
					handleOutput(KVResponse);
				} catch (IOException ioe) {
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
					System.out.println("Error in message ECSConnection");
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
				if (ecsSocket != null) {
					input.close();
					output.close();
					ecsSocket.close();
				}
			} catch (IOException ioe) {
				logger.error("Error! Unable to tear down connection!", ioe);
			}
		}
	}

	private KVAdminMessage handleInput() throws IOException, NullPointerException, ClassNotFoundException {
		KVAdminMessage kvMessage = null;
		try{
			logger.info("Waiting for message from ECS");
			kvMessage = (KVAdminMessage) input.readObject();
		} catch (EOFException e) {
			System.out.println("SEEN EOF");
		}
		logger.info("Received message from ECS " + kvMessage.messageID());
		KVAdminMessage returnMessage = null;
		switch(kvMessage.getStatus()){
			case dummy:
				try {

					logger.info("Received Dummy Message! Sending back dummy reply");
					returnMessage = new KVAdminMessage("dummy reply:)", StatusType.dummyReply);
				} catch (Exception e) {
					logger.error("ERROR DUMMY: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			case INIT:
				try {
					kvServer.initKVServer(kvMessage.getMetadata());
					returnMessage = new KVAdminMessage("", StatusType.INIT_ACK);
					logger.info("INIT Server " + kvServer.getHostname() + ":" + kvServer.getPort());	
				} catch (Exception e) {
					logger.error("ERROR INIT: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			case START :
				try {
					kvServer.initKVServer(kvMessage.getMetadata());
					kvServer.startServer();
					returnMessage = new KVAdminMessage("", StatusType.START_ACK);
					logger.info("START Server " + kvServer.getHostname() + ":" + kvServer.getPort());	
				} catch (Exception e) {
					logger.error("ERROR START: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			case STOP :
				try {
					kvServer.stopServer();
					returnMessage = new KVAdminMessage("", StatusType.STOP_ACK);
					logger.info("STOP Server " + kvServer.getHostname() + ":" + kvServer.getPort());	
				} catch (Exception e) {
					logger.error("ERROR STOP: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			case SHUT_DOWN:
				try {
					kvServer.shutDown();
					returnMessage = new KVAdminMessage("", StatusType.SHUT_DOWN_ACK);
					logger.info("SHUT DOWN Server " + kvServer.getHostname() + ":" + kvServer.getPort());	
				} catch (Exception e) {
					logger.error("ERROR SHUT DOWN: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			case LOCK:
				try {
					kvServer.writeLock();
					returnMessage = new KVAdminMessage("", StatusType.LOCK_ACK);
					logger.info("LOCK Server " + kvServer.getHostname() + ":" + kvServer.getPort());	
				} catch (Exception e) {
					logger.error("ERROR LOCK: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			case UNLOCK:
				try {
					kvServer.unLockWrite();
					returnMessage = new KVAdminMessage("", StatusType.UNLOCK_ACK);
					logger.info("UNLOCK Server " + kvServer.getHostname() + ":" + kvServer.getPort());	
				} catch (Exception e) {
					logger.error("ERROR UNLOCK: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			case UPDATE: //updates metadata
				try {
					kvMessage.printMetadata();
					TreeMap<String, IECSNode> updatedMetaData = kvServer.setMetaData(kvMessage.getMetadata());
					if (updatedMetaData == null) {
						return new KVAdminMessage("Unable to update metadata", StatusType.ERROR, null);
					}
					returnMessage = new KVAdminMessage("", StatusType.UPDATE_ACK, updatedMetaData);
					logger.info("UPDATE Server " + kvServer.getHostname() + ":" + kvServer.getPort());	
				} catch (Exception e) {
					logger.error("ERROR Update: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			case MOVE:
				try {
					kvServer.setMetaData(kvMessage.getMetadata()); //set metadata first
					kvServer.moveData(kvMessage.getDstServerHashName(), kvMessage.getNextUpperBound(),
							kvMessage.getDelete(), kvMessage.getReplicate());
					returnMessage = new KVAdminMessage("", StatusType.MOVE_ACK);
					logger.info("MOVE Server " + kvServer.getHostname() + ":" + kvServer.getPort());	
				} catch (Exception e) {
					logger.error("ERROR Move: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			case DELETE:
				try {
					kvServer.deleteData(kvMessage.getNextUpperBound());
					returnMessage = new KVAdminMessage("", StatusType.DELETE_ACK);
					logger.info("DELETE Server " + kvServer.getHostname() + ":" + kvServer.getPort());
				} catch (Exception e) {
					logger.error("ERROR Delete: " + e.toString());
					returnMessage = new KVAdminMessage(e.toString(), StatusType.ERROR);
				}
				break;
			default:
				logger.error("Error in Received Status Type: " + kvMessage.getStatus());
				String msg = "Error in Received Status Type: " + kvMessage.getStatus();
				returnMessage = new KVAdminMessage(msg, StatusType.ERROR);
				break;
		}

		return returnMessage;
	}

	public void handleOutput(KVAdminMessage kvMessage) throws IOException {
		if (kvMessage == null) return;
		logger.info("Sending message to ecs: " + kvMessage.messageID());
		output.writeObject(kvMessage);
		output.flush();
		logger.info("Successful Sending message to ecs: " + kvMessage.messageID());
	}

}