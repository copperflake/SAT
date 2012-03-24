package sat;

import java.io.ObjectOutputStream;
import java.net.Socket;

import sat.radio.RadioID;
import sat.radio.message.MessageKeepalive;

public abstract class Lab {
	/**
	 * Méthode utilitaire. Cette méthode est utilisée pour executer facilement
	 * un morceau de code lors du développement. Elle n'a aucune utilité dans
	 * l'application finale.
	 */
	public static void lab(String[] args) {
		try {
			Socket sock = new Socket("localhost", 6969);
			ObjectOutputStream ois = new ObjectOutputStream(sock.getOutputStream());

			ois.writeObject(new MessageKeepalive(new RadioID(), 0, 0));
			ois.writeObject(new MessageKeepalive(new RadioID(), 0, 0));

			ois.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
