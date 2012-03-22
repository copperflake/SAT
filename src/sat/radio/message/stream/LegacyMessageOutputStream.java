package sat.radio.message.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sat.crypto.RSAKey;
import sat.radio.message.*;

public class LegacyMessageOutputStream extends MessageOutputStream {
	private ByteArrayOutputStream baos;
	private DataOutputStream dos;

	public LegacyMessageOutputStream(OutputStream out) {
		super(out);

		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
	}

	public void writeMessage(Message m) throws IOException {
		baos.reset();

		dos.write(m.getId().toLegacyId());
		dos.writeInt(m.getLength());
		dos.writeInt(m.getPriority());
		dos.writeInt(m.getPosX());
		dos.writeInt(m.getPosY());

		switch(m.getType()) {
			case HELLO:
				MessageHello hello = (MessageHello) m;

				dos.write(hello.getReserved());
				break;

			case DATA:
				MessageData data = (MessageData) m;

				dos.write(data.getHash());
				dos.writeInt(data.getContinuation());
				dos.write(data.getFormat());
				dos.writeInt(data.getFileSize());
				dos.write(data.getPayload());
				break;

			case MAYDAY:
				MessageMayDay mayday = (MessageMayDay) m;

				dos.writeChars(mayday.getCause());
				break;

			case SENDRSA:
				MessageSendRSAKey sendrsa = (MessageSendRSAKey) m;
				RSAKey key = sendrsa.getKey();

				dos.writeInt(key.getLength());

				byte[] modulus = key.getModulus().toByteArray();
				dos.writeInt(modulus.length);
				dos.write(modulus);

				byte[] exponent = key.getExponent().toByteArray();
				dos.writeInt(exponent.length);
				dos.write(exponent);
				break;

			case ROUTING:
				MessageRouting routing = (MessageRouting) m;

				// TODO: data
				break;

			case CHOKE:
			case UNCHOKE:
			case BYE:
			case KEEPALIVE:
			case LANDINGREQUEST:
				// No additionnal data
				break;

			default:
				// Invalid message, send nothing
				throw new IOException("Invalid message");
		}

		dos.flush(); // useful ?
		out.write(baos.toByteArray());
	}
}
