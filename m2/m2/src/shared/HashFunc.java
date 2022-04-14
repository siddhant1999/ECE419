package shared;

import ecs.IECSNode;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

public class HashFunc {

    public static String hashString(String s)  {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(s.getBytes());
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }

    }

    public static String findNextLargest(String hashedK, TreeMap<String, IECSNode> nodes){
        if(nodes.isEmpty()) return null;
        return (nodes.higherKey(hashedK)==null) ? nodes.firstKey() : nodes.higherKey(hashedK);
    }

    public static String findPrev(String hashedK, TreeMap<String, IECSNode> nodes){
        if(nodes.isEmpty()) return null;
        return (nodes.lowerKey(hashedK)==null) ? nodes.lastKey() : nodes.lowerKey(hashedK);
    }
}
