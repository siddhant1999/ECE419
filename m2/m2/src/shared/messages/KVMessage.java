package shared.messages;

import app_kvServer.KeyValue;
import ecs.IECSNode;

import java.util.LinkedList;
import java.util.TreeMap;

public interface KVMessage {
	
	public enum StatusType {
		GET, 			/* Get - request */
		GET_ERROR, 		/* requested tuple (i.e. value) not found */
		GET_SUCCESS, 	/* requested tuple (i.e. value) found */
		PUT, 			/* Put - request */
		PUT_SUCCESS, 	/* Put - request successful, tuple inserted */
		PUT_UPDATE, 	/* Put - request successful, i.e. value updated */
		PUT_ERROR, 		/* Put - request not successful */
		DELETE_SUCCESS, /* Delete - request successful */
		DELETE_ERROR, 	/* Delete - request successful */

		SERVER_STOPPED,
		SERVER_WRITE_LOCK,
		SERVER_NOT_RESPONSIBLE,
		GET_METADATA, //used for testing
		GET_ALL_DATA,//used for testing
		DELETE_ALL_DATA
	}

	/**
	 * @return the key that is associated with this message, 
	 * 		null if not key is associated.
	 */
	public String getKey();
	
	/**
	 * @return the value that is associated with this message, 
	 * 		null if not value is associated.
	 */
	public String getValue();
	
	/**
	 * @return a status string that is used to identify request types, 
	 * response types and error types associated to the message.
	 */
	public StatusType getStatus();

	public TreeMap<String, IECSNode> getMetadata();

	public void printMetadataKeys();

	public void printKVMessage();

	public String messageID();

	public void printMetadata();

	public boolean getForcePut();

	public boolean getReplicate();

	public boolean getFromClient();

	public LinkedList<KeyValue> getAllData();

}


