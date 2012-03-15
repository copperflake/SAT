package sat.radio.message;

import sat.file.FileFormat;

public class MessageFileSegment extends Message {
	protected MessageType type = MessageType.DATA;
	protected int priority = 0;
	
	private int fileSize;
	private FileFormat format;
	private byte[] hash;
	private int packetNumber;
	private byte[] payload;
	
	public MessageFileSegment() {
		super();
	}
	
	public byte[] getPayload() {
		return this.payload;
	}

	public String toString() {
		return "I'm a FileSegment!";
	}
}
