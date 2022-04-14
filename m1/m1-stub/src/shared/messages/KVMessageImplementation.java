package shared.messages;

public class KVMessageImplementation implements KVMessage {
	String key, value;
	StatusType status;

	public KVMessageImplementation(String key, String value, StatusType status) {
		this.key = key;
		this.value = value;
		this.status = status;
	}
	public KVMessageImplementation(String key, String value, String status) {
		this.key = key;
		this.value = value;
		this.status = StatusType.valueOf(status);
	}


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
}