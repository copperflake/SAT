package sat.radio.message;

public enum MessageType {
	INVALID,
	
	HELLO,
	DATA,
	MAYDAY,
	SENDRSA,
	CHOKE,
	UNCHOKE,
	BYE,
	ROUTING,
	KEEPALIVE,
	LANDINGREQUEST
}
