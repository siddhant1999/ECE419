package ecs;
import shared.HashFunc;

import java.io.*;

public class ECSNode implements IECSNode, Serializable{
	private String name;
	private String host;
	private int port;
	private String hashedName;

	public ECSNode(String host, int port){
		this.name = host+":"+Integer.toString(port);
		this.hashedName = HashFunc.hashString(this.name);
		this.host = host;
		this.port = port;
	}

	 /**
	 * @return  the name of the node (ie "Server 8.8.8.8")
	 */
	public String getNodeName(){return name;}
	
	public String getNodePath(){return "/" + name;}

	/**
	 * @return  the hostname of the node (ie "8.8.8.8")
	 */
	public String getNodeHost(){return host;}

	/**
	 * @return  the port number of the node (ie 8080)
	 */
	public int getNodePort(){return port;}

	public String getHashedName(){return hashedName;}

}
