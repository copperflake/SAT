package sat.radio.message;

import sat.file.FileFormat;
import sat.radio.RadioID;

public class MessageData extends Message {
	private int fileSize;
	private FileFormat format;
	private byte[] hash;
	private int packetNumber;
	private byte[] payload;

	public MessageData(RadioID id, int px, int py) {
		super(id, px, py);

		type = MessageType.DATA;
		priority = 4;
	}

	public byte[] getPayload() {
		return this.payload;
	}

	public String toString() {
		return "I'm a FileSegment!";
	}

	private static final long serialVersionUID = 6788760569267293839L;
}
