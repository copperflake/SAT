package sat.radio.message;

public enum MessageType {
	// ITP Defined
	HELLO,
	DATA,
	MAYDAY,
	SENDRSA,
	CHOKE,
	UNCHOKE,
	BYE,
	ROUTING, 
	KEEPALIVE,
	LANDINGREQUEST,
	
	// ITP Extensions
	INVALID
}
