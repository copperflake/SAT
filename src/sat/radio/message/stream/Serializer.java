package sat.radio.message.stream;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serializer {
	public static byte[] serialize(Object o) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
		}
		catch(IOException e) {
			e.printStackTrace();
			return null;
		}

		return baos.toByteArray();
	}

	public static Object deserialize(byte[] o) {
		ByteArrayInputStream bais = new ByteArrayInputStream(o);

		try {
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		}
		catch(IOException e) {
			e.printStackTrace();
			return null;
		}
		catch(ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
}
