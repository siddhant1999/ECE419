package shared.messages;

import app_kvServer.KeyValue;
import ecs.IECSNode;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class KVMessageImplementation implements KVMessage, Serializable {
	String key, value;
	StatusType status;
	TreeMap<String, IECSNode> metadata;
	boolean forcePut;
	boolean replicate;
	boolean fromClient;
	LinkedList<KeyValue> allData = new LinkedList<>();

	public KVMessageImplementation(String key, String value, StatusType status) {
		this.key = key;
		this.value = value;
		this.status = status;
		this.metadata = new TreeMap<>();
		this.replicate = false;
	}

	public KVMessageImplementation(String key, String value, StatusType status,TreeMap<String,IECSNode> metadata){
		this.key = key;
		this.value = value;
		this.status = status;
		this.metadata = metadata;
		this.replicate = false;
	}

	public KVMessageImplementation(StatusType status,LinkedList<KeyValue> allData){
		this.status = status;
		this.allData = allData;
	}

	public KVMessageImplementation(String key, String value, StatusType status, boolean forcePut, boolean replicate, boolean fromClient) {
		this.key = key;
		this.value = value;
		this.status = status;
		this.metadata = new TreeMap<>();
		this.forcePut = forcePut;
		this.replicate = replicate;
		this.fromClient = fromClient;
	}

	public boolean getReplicate(){return replicate;}
	public boolean getFromClient(){return fromClient;}

	public boolean getForcePut(){return forcePut;}

	public String getKey() {
		return key;
	}
	
	/**
	 * @return the value that is associated with this message, 
	 * 		null if not value is associated.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @return a status string that is used to identify request types, 
	 * response types and error types associated to the message.
	 */
	public StatusType getStatus() {
		return status;
	}

	public TreeMap<String,IECSNode> getMetadata() {return metadata;}

	public void printMetadataKeys(){
		if(metadata.isEmpty()){
			System.out.println("metadata is empty");
			return;
		}
		System.out.print("Metadata ports: ");
		for (String bi : getMetadata().keySet()){
			System.out.print(metadata.get(bi).getNodePort() + " ");
		}
		System.out.print("\n");
	}

	public void printKVMessage(){
		System.out.println("++++++++++Printing KVMessage++++++++++");
		System.out.println("key:"+key + "|val:" + value + "|Status:"+status);
		printMetadataKeys();
		System.out.println("++++++++++Finish Printing KVMessage++++++++++");
	}

	public void printMetadata(){
		System.out.println("++++++++Printing Metadata++++++++");
		for (Map.Entry<String, IECSNode> entry : metadata.entrySet()) {
			IECSNode node = entry.getValue();
			System.out.println("Node: " + node.getNodeName() + " | HashedName: " + node.getHashedName());
		}
		System.out.println("++++++++Finished Printing Metadata++++++++");

	}

	public String messageID(){return "key:"+key + "|val:" + value + "|Status:"+status;}

	@Override
	public LinkedList<KeyValue> getAllData() {
		return allData;
	}
}