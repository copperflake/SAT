package sat.radio.socket;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Un socket de radio direct. Les socket de type direct sont utilisés lorsque le
 * moteur interne travail avec des flux sembables aux flux abstraits des
 * <code>RadioSocket</code>.
 * <p>
 * Dans un tel cas, les flux d'entrée et sortie déjà disponibles sont simplement
 * englobés dans des flux de type <code>RadioSocketInput/OutputStream</code>.
 */
public class RadioSocketDirect extends RadioSocket {
	/**
	 * Crée un nouveau socket direct utilisant des flux d'entrée/sortie déjà
	 * disponibles.
	 * 
	 * @param in
	 *            Le flux d'entrée à utiliser pour ce socket.
	 * @param out
	 *            Le flux de sortie à utiliser pour ce socket.
	 */
	public RadioSocketDirect(InputStream in, OutputStream out) {
		this.in = new RadioSocketInputStream(in);
		this.out = new RadioSocketOutputStream(out);
	}

}
