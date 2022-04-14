package client;
import java.io.*;
import java.net.Socket;

import app_kvClient.KVClient;
import shared.messages.KVMessage;
import shared.messages.KVMessageImplementation;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import shared.Serializer;

public class KVStore implements KVCommInterface {
	/**
	 * Initialize KVStore with address and port of KVServer
	 * @param address the address of the KVServer
	 * @param port the port of the KVServer
	 */
	int port;
	String address;
	boolean running;
	
	private OutputStream output;
 	private InputStream input;

	private Socket clientSocket;
	public Logger logger = Logger.getRootLogger();

	public KVStore(String address, int port) {
		this.port = port;
		this.address = address;
		this.running = true;
	}

	@Override
	public void connect() throws Exception {
		// TODO Auto-generated method stub
		clientSocket = new Socket(address, port);
		try {
			input = clientSocket.getInputStream();
			output = clientSocket.getOutputStream();
			running = true;
		} catch (IOException e) {
			logger.error("Connection could not be established!");
		}

	}

	private void tearDownConnection() throws IOException {
		running = false;
		logger.info("tearing down the connection ...");
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
		logger.info("try to disconnect connection ...");

		try {
			tearDownConnection();
		} catch (IOException ioe) {
			logger.error("Unable to close connection!");
		}
	}

	public KVMessage receiveMessage() throws IOException {
		BufferedReader inStream = new BufferedReader(new InputStreamReader(input));
		KVMessage kvMessage = Serializer.deserialize(inStream);
		if (kvMessage == null){
			throw new IOException("Connection Closed");
		}
		logger.info("RECEIVED (k,v,status): (" +
				kvMessage.getKey() + ", " + kvMessage.getValue() + ", " + kvMessage.getStatus()+")");
		return kvMessage;
	}

	@Override
	public KVMessage put(String key, String value) throws IOException {
		KVMessage kvMessage = new KVMessageImplementation(key, value, KVMessage.StatusType.PUT);
		sendMessage(kvMessage);
		return receiveMessage();
	}

	@Override
	public KVMessage get(String key) throws IOException {
		KVMessage kvMessage = new KVMessageImplementation(key, null, KVMessage.StatusType.GET);
		sendMessage(kvMessage);
		return receiveMessage();
	}
	public void sendMessage(KVMessage kvMessage) throws IOException {
		logger.info("SENDING (k,v,status): (" +
				kvMessage.getKey() + ", " + kvMessage.getValue() + ", " + kvMessage.getStatus()+")");
		byte[] msg = Serializer.serialize(kvMessage);
		output.write(msg, 0, msg.length);
		output.flush();
	}
}
