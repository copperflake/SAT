package sat.radio.server;

import sat.radio.RadioDelegate;
import sat.radio.RadioID;

/**
 * Interface d'un délégué d'un <code>RadioServer</code>.
 */
public interface RadioServerDelegate extends RadioDelegate {
	/**
	 * Un avion a terminé sa connexion avec la tour de contrôle. Lors de
	 * l'emission de cet évenement, l'avion a complétement terminé sa connexion
	 * et est disponible pour la réception de messages de la tour.
	 * <p>
	 * La tour n'a pas besoin de se préoccuper des détails techniques comme la
	 * gestion du chiffrement ou le passage en mode étendu.
	 * 
	 * @param plane
	 *            Le RadioID de l'avion nouvellement connecté.
	 */
	public void onPlaneConnected(RadioID plane);

	/**
	 * Un avion a été déconnecté de la tour. Cet évenement est envoyé lorsque la
	 * connexion à un avion a été perdue. Lors de l'appel de la méthode
	 * <code>kick</code>, cet évenement est également généré. Il s'agit donc
	 * d'une façon sûr de surveiller la déconnexion d'un avion quelque soit sa
	 * cause: cet évenement sera toujours émis.
	 * <p>
	 * Après l'émission de cet évenement, plus aucun message ne doit être envoyé
	 * à l'avion puisque celui-ci n'est plus disponible dans la liste des avions
	 * connectés de la radio.
	 * 
	 * @param plane
	 *            Le RadioID de l'avion déconnecté.
	 */
	public void onPlaneDisconnected(RadioID plane);
}
