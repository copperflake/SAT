package sat.radio.message.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sat.radio.message.*;
import sat.utils.crypto.RSAKey;
import sat.utils.geo.Coordinates;

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

		dos.write(m.getID().toLegacyID());
		dos.writeInt(m.getLength());
		dos.writeInt(m.getPriority());

		Coordinates c = m.getCoordinates();

		dos.writeInt(c.getX());
		dos.writeInt(c.getY());

		switch(m.getType()) {
			case HELLO:
				MessageHello hello = (MessageHello) m;

				byte reserved = 0;
				reserved += hello.isCiphered() ? 1 << 4 : 0;
				reserved += hello.isExtended() ? 1 << 7 : 0;

				dos.write(reserved);
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
