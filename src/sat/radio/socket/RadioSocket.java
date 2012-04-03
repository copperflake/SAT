package sat.radio.socket;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Socket spécifique aux classe Radio de SAT.
 * <p>
 * Ces sockets sont une abtraction des méchanisme d'entrée/sortie sous-jacent.
 * Quelque soit le fonctionnement interne des moteurs, ceux-ci fournissent à la
 * radio une interface standard définie par cette classe.
 * <p>
 * Un RadioSocket englobe deux flux (un d'entrée et un de sortie) vers un client
 * Radio spécifique. Ils diffèrent des Sockets Java en exposant directement ces
 * deux flux avec les attributs <code>in</code> et <code>out</code>. Ceci dans
 * le but de simplifier leur utilisation dans les classes de type
 * <code>RadioServer</code>/<code>RadioClient</code>.
 * <p>
 * Les flux interne sont des flux proxy supportant l'upgrading, c'est à dire la
 * modification du flux interne dynamiquement après l'initialisation du flux
 * proxy. Ce méchanisme est utilisé pour ajouter dynamiquement le support du
 * chiffrement par dessus un socket déjà ouvert.
 */
public abstract class RadioSocket {
	/**
	 * Le flux d'entrée de ce socket.
	 */
	public RadioSocketInputStream in;

	/**
	 * Le flux de sortie de ce socket.
	 */
	public RadioSocketOutputStream out;

	/**
	 * Ferme ce socket, ce qui provoque la fermeture des flux d'entrée/sortie
	 * sous-jacents.
	 * 
	 * @throws IOException
	 *             La fermeture d'un flux peut générer une exception.
	 */
	public void close() throws IOException {
		in.close();
		out.close();
	}

	/**
	 * Un flux d'entrée de socket. Ce flux englobe simplement un autre flux vers
	 * lequel il transmet les opérations. Il supporte également l'upgrading
	 * permettant de modifier dynamiquement le flux interne utilisé.
	 */
	public static class RadioSocketInputStream extends FilterInputStream {
		public RadioSocketInputStream(InputStream in) {
			super(in);
		}

		public InputStream getStream() {
			return in;
		}

		public void upgrade(InputStream in) {
			this.in = in;
		}
	}

	/**
	 * Un flux de sortie de socket. Ce flux englobe simplement un autre flux
	 * vers lequel il transmet les opérations. Il supporte également l'upgrading
	 * permettant de modifier dynamiquement le flux interne utilisé.
	 */
	public static class RadioSocketOutputStream extends FilterOutputStream {
		public RadioSocketOutputStream(OutputStream out) {
			super(out);
		}

		public OutputStream getStream() {
			return out;
		}

		public void upgrade(OutputStream out) {
			this.out = out;
		}
	}
}
