package sat.utils.pftp;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sat.DebugEvent;
import sat.plane.PlaneType;
import sat.radio.RadioID;
import sat.radio.message.MessageData;
import sat.utils.file.DataFile;

/**
 * Agent de tranfert de fichier. Un agent est responsable de chaque transfert de
 * fichier, il ordonne les paquets reçus et vérifie que le fichier reçu est
 * correct par rapport au hash annoncé.
 * 
 * Il dépend d'une classe FileTransferAgentDispatcher et ne devrait pas être
 * utilisé directement.
 */
public class FileTransferAgent {
	/**
	 * Le Hash attendu à la fin de la transmission.
	 */
	private Hash hash;

	/**
	 * Nombre de segments à recevoir.
	 */
	private int segmentCount;

	/**
	 * Nombre de segments reçus. En absence de notification de fin de
	 * transmission, ce compteur est utilisé pour déterminer que tous les
	 * messages Data ont été reçus.
	 */
	private int segmentReceived = 0;

	/**
	 * Le chemin du fichier reçu.
	 */
	private String path;

	/**
	 * Le gestionnaire de lecture / écriture dans une fichier segmentable.
	 */
	private DataFile file;

	/**
	 * L'AgentDispatcher auquel cet agent est associé.
	 */
	private FileTransferAgentDispatcher dispatcher;

	/**
	 * Crée un nouvel agent de transfert de ficheir.
	 * 
	 * @param dispatcher
	 *            Le dispatcher associé.
	 * @param hash
	 *            Le hash attendu.
	 * @param sender
	 *            L'ID de l'émetteur.
	 * @param format
	 *            Le format de fichier (extension)
	 * @param size
	 *            La longueur totale du fichier
	 * 
	 * @throws NoSuchAlgorithmException
	 *             Si la plateforme ne supporte pas SHA-1
	 * @throws IOException
	 *             Si la lecture / écriture vers le fichier provoque une
	 *             exception.
	 */
	public FileTransferAgent(FileTransferAgentDispatcher dispatcher, Hash hash, RadioID sender, String format, int size) throws NoSuchAlgorithmException, IOException {
		this.dispatcher = dispatcher;
		this.hash = hash;

		segmentCount = DataFile.segmentsCountForSize(size);

		String filename = sender + "-" + hash.asHex() + "." + format;
		filename = filename.replaceAll("[:/\\\\]", "_");
		path = dispatcher.getDownloadsPath() + filename;

		dispatcher.debugEvent(new DebugEvent("[PFTP] Started receiving  " + path));

		file = new DataFile(path);

		// TODO: implements timeouts
	}

	/**
	 * Termine ce gestionnaire de transfert et notifie le dispatcher de cet
	 * arrêt.
	 */
	public void exit() throws IOException {
		dispatcher.agentExited(hash);
		file.close();
	}

	/**
	 * Termine ce gestionanire de façon innopinée. Le transfert de fichier est
	 * considéré invalide et les données déjà reçues sont effacées.
	 */
	public void abort() throws IOException {
		exit();
		file.delete();
	}

	/**
	 * S'occupe de la réception d'un message particulier.
	 */
	public void gotMessage(MessageData m) throws IOException {
		// Write segment to disk
		file.writeSegment(m.getContinuation(), m.getPayload());

		// Catch PLANE_TYPE= files
		if(m.getContinuation() == 0 && segmentCount == 1) {
			String data = new String(m.getPayload());

			Pattern pattern = Pattern.compile("^PLANE_TYPE=(.+);");
			Matcher matcher = pattern.matcher(data);

			if(matcher.find()) {
				PlaneType type = PlaneType.getPlaneTypeByName(matcher.group(1));

				if(type != null) {
					dispatcher.planeIdentified(m.getID(), type);
					dispatcher.debugEvent(new DebugEvent("[PFTP] Successfully identified " + m.getID() + " as " + type));

					abort(); // Dont need to keep this file
				}
			}
		}

		// TODO: better "file received management"
		if(++segmentReceived >= segmentCount) {
			try {
				Hash receivedHash = new Hash(file.getHash());

				// Checking hashes
				if(!receivedHash.equals(hash)) {
					dispatcher.debugEvent(new DebugEvent("[PFTP] File corrupted from " + m.getID()));

					abort();
					return;
				}
			}
			catch(NoSuchAlgorithmException e) {
				dispatcher.debugEvent(new DebugEvent("[PFTP] Error while hashing file from " + m.getID()));

				abort();
				return;
			}

			dispatcher.debugEvent(new DebugEvent("[PFTP] Successfully received from " + m.getID()));
			dispatcher.transfertComplete(path);
			exit();
		}
	}
}
