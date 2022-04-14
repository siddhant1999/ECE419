package shared;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Metadata class represents response for server to communicate to client
 * DEPRECATED
 */
public class Metadata implements Serializable {
    private int port;
    private String address;
    private BigInteger startHash, endHash;
    //type for server state

    public Metadata(int port, String address, BigInteger startHash, BigInteger endHash) {
        this.port = port;
        this.address = address;
        // this.writeLocked = writeLocked;
        this.startHash = startHash;
        this.endHash = endHash;
    }




    public int getPort(){return port;}
    public String getAddress(){return address;}
    // public boolean getWriteLocked(){return writeLocked;}
    public BigInteger getStartHash(){return startHash;}
    public BigInteger getEndHash(){return endHash;}

    public String getNodeName() {return endHash.toString();}
    public String getNodeHost() {return getAddress();}
    public int getNodePort() {return getPort();}
    public String[] getNodeHashRange() {
        String[] hashRange = {startHash.toString(), endHash.toString()};
        return hashRange;
    }


}
