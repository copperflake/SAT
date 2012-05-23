package sat.utils.pftp;

import sat.DebugEvent;
import sat.plane.PlaneType;
import sat.radio.RadioID;

public interface FileTransferDelegate {
	public String getDownloadsPath();
	public void planeIdentified(RadioID id, PlaneType type);
	public void debugEvent(DebugEvent ev);
}
