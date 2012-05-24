package sat.utils.pftp;

import sat.DebugEvent;
import sat.plane.PlaneType;
import sat.radio.RadioID;

/**
 * Délégué de transfert de fichier. Il est chargé de de gérer les événements
 * survenant lors du transfert de fichiers.
 */
public interface FileTransferDelegate {
	/**
	 * Retourne le préfix à utiliser pour déterminer le chemin de sauvegarde des
	 * fichiers reçus.
	 */
	public String getDownloadsPath();

	/**
	 * Annonce un avion correctement identifié.
	 * 
	 * @param id
	 *            L'ID de l'avion identifié.
	 * @param type
	 *            Son type.
	 */
	public void planeIdentified(RadioID id, PlaneType type);

	/**
	 * Annonce l'émission d'un événement de debuggage.
	 */
	public void debugEvent(DebugEvent ev);

	public void transferComplete(String path);
}
