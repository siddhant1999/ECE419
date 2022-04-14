package shared;

import ecs.IECSNode;

import java.io.*;

public class Serializer {

    public static byte[] nodeToBytes(IECSNode node) {
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(node);
            out.flush();
            byte[] data = bos.toByteArray();
            return data;
        } catch (IOException ioe) {
            System.out.println("Unable to seralize node into bytes");
            ioe.printStackTrace();
            return null;
        }
    }

    public static IECSNode bytesToNode(byte[] data) {
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            ObjectInput in = new ObjectInputStream(bis);
            IECSNode node = (IECSNode) in.readObject();

            in.close();
            return node;
        } catch (IOException ioe) {
            System.out.println("Unable to seralize node into bytes");
            ioe.printStackTrace();
            return null;
        } catch (ClassNotFoundException cnf) {
            System.out.println("Unable to seralize node into bytes");
            cnf.printStackTrace();
            return null;
        }

    }
}
