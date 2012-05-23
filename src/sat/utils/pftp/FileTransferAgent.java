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

public class FileTransferAgent {
	private Hash hash;

	private int segmentCount;
	private int segmentReceived = 0;

	private String path;
	private DataFile file;

	private FileTransferAgentDispatcher dispatcher;

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

	public void exit() throws IOException {
		dispatcher.agentExited(hash);
		file.close();
	}

	public void abort() throws IOException {
		exit();
		file.delete();
	}

	public void gotMessage(MessageData m) throws IOException {
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
					
					abort();
				}
			}
		}

		// TODO: better "file received management"
		if(++segmentReceived >= segmentCount) {
			try {
				Hash receivedHash = new Hash(file.getHash());

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
			exit();
		}
	}
}
