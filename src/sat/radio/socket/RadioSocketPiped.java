package sat.radio.socket;

import sat.EndOfWorldException;

/**
 * [NYI] Un socket piped. Ce type de socket est utilisé lorsque le moteur de la
 * radio utilise des mécanismes internes qui ne correspondent pas au modèle des
 * <code>RadioSocket</code>.
 * <p>
 * Ceci serait par exemple le cas d'un moteur UDP qui ne possède qu'un flux
 * d'entrée et de sortie et gère son multiplexage aux différents clients.
 * <p>
 * Ce type de socket n'est pas encore implémenté et son interface est indéfinie.
 */
public class RadioSocketPiped extends RadioSocket {
	/**
	 * [NYI] Crée un nouveau socket de type piped. Retourne actuellement une
	 * exception car cet objet n'est pas encore implémenté.
	 */
	public RadioSocketPiped() {
		throw new EndOfWorldException("Not Yet Implemented!");
	}
}
