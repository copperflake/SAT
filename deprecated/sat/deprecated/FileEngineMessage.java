package sat.deprecated;

import java.io.Serializable;

import sat.radio.RadioID;

public class FileEngineMessage implements Serializable {
	public RadioID sender;
	public byte[] payload;

	public FileEngineMessage(RadioID sender, byte[] payload) {
		this.sender = sender;
		this.payload = payload;
	}

	private static final long serialVersionUID = -4236893024964936440L;
}
