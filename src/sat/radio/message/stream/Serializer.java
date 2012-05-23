package sat.radio.message.stream;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Un sérialisateur d'objet.
 * 
 * Les classes de sérialisation de Java (ObjectI/OStream) ne fonctionnent que
 * sous la forme de flux. Cette classe fournit deux méthodes permettant de
 * travailler avec des byte[].
 */
public class Serializer {
	/**
	 * Sérialise un objet.
	 * 
	 * @param o
	 *            L'objet à sérialiser.
	 * @return L'objet sérialisé
	 */
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

	/**
	 * Désérialise un objet.
	 * 
	 * @param o
	 *            L'objet sérialisé
	 * @return L'objet désérialisé
	 */
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
