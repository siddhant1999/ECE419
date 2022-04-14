package shared.messages;

import ecs.IECSNode;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class KVAdminMessage implements Serializable{

	public String message;
	public StatusType status;
	public TreeMap<String, IECSNode> metadata;
	public String dstServerHashName, nextUpperBound;
	public boolean delete, replicate;

	public enum StatusType {
		INIT,
		INIT_ACK,
		START,
		START_ACK, 
		STOP,
		STOP_ACK, 
		SHUT_DOWN, 
		SHUT_DOWN_ACK,
		LOCK, 
		LOCK_ACK, 
		UNLOCK, 
		UNLOCK_ACK,
		UPDATE,
		UPDATE_ACK,
		MOVE, 
		MOVE_ACK,
		ERROR,
		DELETE,
		DELETE_ACK,
		dummy, //TODO: Deprecate dummy
		dummyReply
	}

	
	/**
	 * Generic AdminMessage
	 */
	public KVAdminMessage(String message, StatusType status){
		this.message = message;
		this.status = status;
		this.metadata = null;
		this.dstServerHashName = null;
		delete = true;
	}

	/**
	 * Usage for MOVE data case
	 */
	public KVAdminMessage(String message, StatusType status, String dstServerHashName, String nextUpperBound,
						  TreeMap<String, IECSNode> metadata, boolean delete, boolean replicate){
		this.message = message;
		this.status = status;
		this.dstServerHashName = dstServerHashName;
		this.metadata = metadata;
		this.delete = delete;
		this.replicate = replicate;
		this.nextUpperBound = nextUpperBound;
	}

	/**
	 * Usage for UPDATE metadata case
	 */
	public KVAdminMessage(String message, StatusType status, TreeMap<String, IECSNode> metadata){
		this.message = message;
		this.status = status;
		this.metadata = metadata;
		this.dstServerHashName = null;
		delete = true;
	}

	public String getNextUpperBound(){return nextUpperBound;}

	public String messageID(){return "msg: "+ this.message + "| of status: " + this.status;}

	public String getMessage() {
		return this.message;
	}

	public StatusType getStatus() {
		return this.status;
	}

	public TreeMap<String, IECSNode> getMetadata() {
		return this.metadata;
	}

	public String getDstServerHashName(){
		return this.dstServerHashName;
	}

	public boolean getDelete(){return delete;}
	public boolean getReplicate(){return replicate;}

	public void printKVMessage(){
		System.out.println("msg: "+ this.message + "| of status: " + this.status);
	}

	public void printMetadata(){
		System.out.println("++++++++Printing Metadata++++++++");
		if (metadata == null ) {
			System.out.println("Metadata null");
			return;
		}
		for (Map.Entry<String, IECSNode> entry : metadata.entrySet()) {
			IECSNode node = entry.getValue();
			System.out.println("Node: " + node.getNodeName() + " | HashedName: " + node.getHashedName());
		}
		System.out.println("++++++++Finished Printing Metadata++++++++");

	}



}