package sat.radio.message.stream;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import sat.radio.message.Message;
import sat.radio.message.MessageHello;
import sat.radio.message.MessageType;

public class MessageInputStream extends FilterInputStream {
	private DataInputStream dis;
	
	public MessageInputStream(InputStream in) {
		super(in);
		dis = new DataInputStream(in);
	}
	
	public Message readMessage() throws IOException {
		byte[] planeID = fill(new byte[8]);
		
		int length = dis.readInt();
		int priority = dis.readInt();
		int posx = dis.readInt();
		int posy = dis.readInt();
		
		MessageType type = MessageType.values()[dis.readInt()];
		
		Message message;
		
		switch(type) {
			case HELLO:
				byte reserved = dis.readByte();
				//message = new MessageHello(reserved);
				break;
				
			case DATA:
				break;
				
			case MAYDAY:
				break;
				
			case SENDRSA:
				int keySize = dis.readInt();
				
				int modulusLength = dis.readInt();
				byte[] modulus = fill(new byte[modulusLength]);
				
				int publicKeyLength = dis.readInt();
				byte[] publicKey = fill(new byte[publicKeyLength]);
				
				break;
				
			case CHOKE:
				break;
				
			case UNCHOKE:
				break;
				
			case BYE:
				break;
				
			case ROUTING:
				break;
				
			case KEEPALIVE:
				break;
				
			case LANDINGREQUEST:
				break;
		}
		
		return null;
	}
	
	private byte[] fill(byte[] buffer) throws IOException {
		for(int i = 0; i < buffer.length; i++) {
			buffer[i] = dis.readByte();
		}
		
		return buffer;
	}
}
