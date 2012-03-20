package sat.radio.message;

import sat.file.FileFormat;

public class MessageData extends Message {
	protected MessageType type = MessageType.DATA;
	protected int priority = 4;

	private int fileSize;
	private FileFormat format;
	private byte[] hash;
	private int packetNumber;
	private byte[] payload;

	public MessageData() {
		super();
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public String toString() {
		return "I'm a FileSegment!";
	}

	private static final long serialVersionUID = 6788760569267293839L;
}
