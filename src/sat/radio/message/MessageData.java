package sat.radio.message;

import sat.radio.RadioID;
import sat.utils.geo.Coordinates;

@SuppressWarnings("serial")
public class MessageData extends Message {
	private byte[] hash;
	int continuation;
	private String format;
	private int fileSize;
	private byte[] payload;

	public MessageData(RadioID id, Coordinates c, byte[] hash, int continuation, String format, int fileSize, byte[] payload) {
		super(id, c);

		type = MessageType.DATA;
		priority = 4;

		this.hash = hash;
		this.continuation = continuation;
		this.format = format;
		this.fileSize = fileSize;
		this.payload = payload;

		length = payload.length;
	}

	public byte[] getHash() {
		return hash;
	}

	public int getContinuation() {
		return continuation;
	}

	public String getFormat() {
		return format;
	}

	public int getFileSize() {
		return fileSize;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public String toString() {
		return "I'm a FileSegment!";
	}
}
