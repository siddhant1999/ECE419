package shared;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class SocketLib {
    public Socket socket;
    public ObjectInputStream input;
    public ObjectOutputStream output;

    public SocketLib(Socket socket, ObjectInputStream input, ObjectOutputStream output){
        this.socket = socket;
        this.input = input;
        this.output = output;
    }
}
