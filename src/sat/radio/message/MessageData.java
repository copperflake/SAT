package sat.radio.message;

import sat.radio.RadioID;

public class MessageData extends Message {
	private byte[] hash;
	int continuation;
	private byte[] format;
	private int fileSize;
	private byte[] payload;

	public MessageData(RadioID id, int px, int py, byte[] hash, int continuation, byte[] format, int fileSize, byte[] payload) {
		super(id, px, py);

		this.hash = hash;
		this.continuation = continuation;
		this.format = format;
		this.fileSize = fileSize;
		this.payload = payload;
	}

	public void resetTypeAndPriority() {
		type = MessageType.DATA;
		priority = 4;
	}

	public byte[] getHash() {
		return hash;
	}

	public int getContinuation() {
		return continuation;
	}

	public byte[] getFormat() {
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

	private static final long serialVersionUID = 6788760569267293839L;
}
