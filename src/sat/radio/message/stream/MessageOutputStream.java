package sat.radio.message.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sat.radio.message.*;
import sat.utils.crypto.RSAKey;
import sat.utils.geo.Coordinates;

public class MessageOutputStream extends FilterOutputStream {
	private ByteArrayOutputStream baos;
	private DataOutputStream dos;

	private boolean extended = false;

	public MessageOutputStream(OutputStream out) {
		super(out);

		baos = new ByteArrayOutputStream();
		dos = new DataOutputStream(baos);
	}

	public synchronized void writeMessage(Message m) throws IOException {
		baos.reset();

		if(extended) {
			byte[] id = Serializer.serialize(m.getID());
			dos.writeInt(id.length);
			dos.write(id);
		}
		else {
			dos.write(m.getID().toLegacyID());
		}

		dos.writeInt(m.getLength());
		dos.writeInt(m.getPriority());

		Coordinates c = m.getCoordinates();

		dos.writeInt((int) c.getX());
		dos.writeInt((int) c.getY());

		dos.writeInt(m.getType().ordinal());

		switch(m.getType()) {
			case HELLO:
				writeMessageAttributes((MessageHello) m);
				break;

			case DATA:
				writeMessageAttributes((MessageData) m);
				break;

			case MAYDAY:
				writeMessageAttributes((MessageMayDay) m);
				break;

			case SENDRSA:
				writeMessageAttributes((MessageSendRSAKey) m);
				break;

			case ROUTING:
				writeMessageAttributes((MessageRouting) m);
				break;

			case CHOKE:
			case UNCHOKE:
			case BYE:
			case KEEPALIVE:
			case LANDINGREQUEST:
			case UPGRADE:
				// No additionnal data
				break;

			default:
				// Invalid message, send nothing
				throw new IOException("Invalid message");
		}

		dos.flush(); // useful ?
		out.write(baos.toByteArray());
		out.flush();
	}

	private void writeMessageAttributes(MessageHello m) throws IOException {
		byte reserved = 0;

		reserved += m.isCiphered() ? 1 << 4 : 0;
		reserved += m.isExtended() ? 1 << 7 : 0;

		dos.write(reserved);
	}

	private void writeMessageAttributes(MessageData m) throws IOException {
		dos.write(m.getHash());
		dos.writeInt(m.getContinuation());
		dos.write(m.getFormat());
		dos.writeInt(m.getFileSize());
		dos.write(m.getPayload());
	}

	private void writeMessageAttributes(MessageMayDay m) throws IOException {
		dos.writeChars(m.getCause());
	}

	private void writeMessageAttributes(MessageSendRSAKey m) throws IOException {
		RSAKey key = m.getKey();

		dos.writeInt(key.getLength());

		byte[] modulus = key.getModulus().toByteArray();
		dos.writeInt(modulus.length);
		dos.write(modulus);

		byte[] exponent = key.getExponent().toByteArray();
		dos.writeInt(exponent.length);
		dos.write(exponent);
	}

	private void writeMessageAttributes(MessageRouting m) throws IOException {
		// TODO
	}

	public boolean isExtended() {
		return extended;
	}

	public synchronized void setExtended(boolean extended) {
		this.extended = extended;
	}
}
