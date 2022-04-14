package shared;

import shared.messages.KVMessage;
import shared.messages.KVMessageImplementation;

import java.io.BufferedReader;
import java.io.IOException;

public class Serializer {
	public static byte[] serialize(KVMessage KV) {
		String serializedStr = KV.getStatus().name() + "\n" + KV.getKey() + "\n" + KV.getValue() + "\n\n";
		return serializedStr.getBytes();
	}

	public static KVMessage deserialize(BufferedReader inStream) throws IOException {
		int i = 0;
		String line;
		String lines [] = new String[3];
		while((line = inStream.readLine()) != null && i < 3) {
			lines[i] = line;
			i++;
		}
		if (lines[0].equals(null)){return null;}
		return new KVMessageImplementation(getString(lines[1]),getString(lines[2]),getString(lines[0]));
	}

	private static String getString(String s ){return (s == "null") ? null : s;}

}
