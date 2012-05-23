package sat.utils.pftp;

import java.io.IOException;
import java.util.HashMap;

import sat.DebugEvent;
import sat.plane.PlaneType;
import sat.radio.RadioID;
import sat.radio.message.MessageData;

public class FileTransferAgentDispatcher {
	private HashMap<Hash, FileTransferAgent> agents = new HashMap<Hash, FileTransferAgent>();
	private FileTransferDelegate delegate;
	
	public FileTransferAgentDispatcher(FileTransferDelegate delegate) {
		this.delegate = delegate;
	}
	
	public synchronized void dispatchMessageToAgent(MessageData m) {
		Hash hash = new Hash(m.getHash());

		FileTransferAgent agent = agents.get(hash);

		if(agent == null) {
			try {
				agent = new FileTransferAgent(this, hash, m.getID(), m.getFormat(), m.getFileSize());
			}
			catch(Exception e) {
				e.printStackTrace();
				return;
			}

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

	public synchronized void deleteAgent(Hash hash) {
		agents.remove(hash);
	}
	
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
}