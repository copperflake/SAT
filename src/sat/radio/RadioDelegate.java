package sat.radio;

import sat.radio.message.Message;
import sat.utils.geo.Coordinates;

/**
 * Délégué d'une radio générique. Cette inteface défini les méthodes utilisées à
 * la fois par les délégués de type <code>RadioServerDelegate</code> et
 * <code>RadioClientDelegate</code>.
 */
public interface RadioDelegate {
	/**
	 * Demande au délégué sa position.
	 */
	public Coordinates getLocation();

	/**
	 * Annonce au délégué la réception d'un message.
	 * 
	 * @param sender
	 *            Le RadioID de l'émetteur de ce message
	 * @param message
	 *            Le message reçu.
	 */
	public void onMessage(RadioID sender, Message message);
}
