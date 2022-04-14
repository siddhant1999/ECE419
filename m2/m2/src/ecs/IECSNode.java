package ecs;

import shared.messages.KVAdminMessage;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;

public interface IECSNode {
    /**
     * @return  the name of the node (ie "Server 8.8.8.8")
     */
    public String getNodeName();

    public String getNodePath();

    /**
     * @return  the hostname of the node (ie "8.8.8.8")
     */
    public String getNodeHost();

    /**
     * @return  the port number of the node (ie 8080)
     */
    public int getNodePort();

    public String getHashedName();

}
