package sat.utils.pftp;

import java.io.IOException;
import java.util.HashMap;

import sat.DebugEvent;
import sat.plane.PlaneType;
import sat.radio.RadioID;
import sat.radio.message.MessageData;

/**
 * Dispatcher de tranfert de fichier. Cette classe se charge de gérer un groupe
 * de FileTransferAgent, chacun associé à un transfert particulier.
 */
public class FileTransferAgentDispatcher {
	/**
	 * Liste des agents disponibles en fonction des hash des différents
	 * fichiers.
	 */
	private HashMap<Hash, FileTransferAgent> agents = new HashMap<Hash, FileTransferAgent>();

	/**
	 * Le délégué de ce dispatcher.
	 */
	private FileTransferDelegate delegate;

	/**
	 * Crée un nouveau dispatcher lié à un délégué donné.
	 */
	public FileTransferAgentDispatcher(FileTransferDelegate delegate) {
		this.delegate = delegate;
	}

	/**
	 * Sélectionne l'agent approprié pour le traitement d'un message MessageData
	 * et lui transmet le message reçu. Si un tel agent n'existe pas, il est
	 * automatiquement initialisé.
	 */
	public synchronized void dispatchMessageToAgent(MessageData m) {
		// Wrapping the raw hash
		Hash hash = new Hash(m.getHash());

		FileTransferAgent agent = agents.get(hash);

		if(agent == null) {
			// No agent available for this file...
			try {
				agent = new FileTransferAgent(this, hash, m.getID(), m.getFormat(), m.getFileSize());
			}
			catch(Exception e) {
				e.printStackTrace();
				return;
			}

			// Register agent
			agents.put(hash, agent);
		}

		try {
			agent.gotMessage(m);
		}
		catch(IOException e) {
			debugEvent(new DebugEvent("[PFTP] Failed to write data block from " + m.getID() + " file transfer aborted"));

			try {
				agent.abort();
			}
			catch(IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Supprime un agent de la liste d'agent de ce dispatcher. Cette méthode est
	 * appelée automatiquement par l'agent lui-même quand sa tâche est terminée.
	 */
	public synchronized void deleteAgent(Hash hash) {
		agents.remove(hash);
	}

	/**
	 * Supprime un agent de la liste d'agent de ce dispatcher. Cette méthode est
	 * appelée automatiquement par l'agent lui-même quand sa tâche est terminée.
	 */
	// TODO : what's that ?
	public void agentExited(Hash hash) {
		agents.remove(hash);
	}

	// - - - Forward to delegate - - -

	public String getDownloadsPath() {
		return delegate.getDownloadsPath();
	}

	public void debugEvent(DebugEvent ev) {
		delegate.debugEvent(ev);
	}

	public void planeIdentified(RadioID id, PlaneType type) {
		delegate.planeIdentified(id, type);
	}
	
	public void transfertComplete(String path) {
		delegate.transferComplete(path);
	}
}
