package sat.radio.message;

/**
 * Les types de message possible. Les premiers éléments de cette énumération
 * sont les type de message réservés par le protocole officiel de l'ITP. Suivis
 * des types supplémentaires utilisés par le protocole étendu.
 */
public enum MessageType {
	// ITP Defined
	HELLO, DATA, MAYDAY, SENDRSA, CHOKE, UNCHOKE, BYE, ROUTING, KEEPALIVE, LANDINGREQUEST,

	// ITP Reserved
	ITP_RESERVED_1, ITP_RESERVED_2, ITP_RESERVED_3, ITP_RESERVED_4, ITP_RESERVED_5,

	// ITP Extensions
	INVALID, UPGRADE
}
