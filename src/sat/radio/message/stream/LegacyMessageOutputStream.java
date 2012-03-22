package sat.radio.message.stream;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
		dos.writeInt(m.getPosX());
		dos.writeInt(m.getPosY());

		switch(m.getType()) {
			case HELLO:
				MessageHello hello = (MessageHello) m;
				break;

			case DATA:
				MessageData data = (MessageData) m;
				break;

			case MAYDAY:
				MessageMayDay mayday = (MessageMayDay) m;
				break;

			case SENDRSA:
				MessageSendRSAKey sendrsa = (MessageSendRSAKey) m;
				break;

			case CHOKE:
				MessageChoke choke = (MessageChoke) m;
				break;

			case UNCHOKE:
				MessageUnchoke unchoke = (MessageUnchoke) m;
				break;

			case BYE:
				MessageBye bye = (MessageBye) m;
				break;

			case ROUTING:
				MessageRouting routing = (MessageRouting) m;
				break;

			case KEEPALIVE:
				MessageKeepalive keepalive = (MessageKeepalive) m;
				break;

			case LANDINGREQUEST:
				MessageLanding landing = (MessageLanding) m;
				break;

			default:
				// Invalid message, send nothing
				throw new IOException("Invalid message");
		}

		dos.flush(); // useful ?
		out.write(baos.toByteArray());
	}
}
