package sat.utils.pftp;

import java.util.Arrays;

/**
 * Wrapper de Hash. Les hashs sont des tableaux de bytes. Ils ne peuvent donc
 * pas être comparés facilement, ni utilisés comme clé de Map. Deux
 * fonctionnalités requises par le système de transfert de fichiers. Cette
 * classe fournit les méthodes nécessaire à la comparaison et l'utilisation
 * comme clé.
 */
public class Hash {
	/**
	 * Le hash encapsulé dans cette classe.
	 */
	private byte[] hash;

	/**
	 * Construit un nouveau wrapper autour d'un hash.
	 */
	public Hash(byte[] hash) {
		this.hash = hash;
	}

	/**
	 * Vérifie l'égalité de deux Hashs.
	 */
	public boolean equals(Object o) {
		// Obvious equality
		if(this == o)
			return true;

		// Obvious inequality
		if((o == null) || (o.getClass() != this.getClass()))
			return false;

		Hash h = (Hash) o;

		return Arrays.equals(hash, h.hash);
	}

	/**
	 * Génère un hashCode pour l'utilisation dans une HashMap.
	 */
	public int hashCode() {
		return Arrays.hashCode(hash);
	}

	/**
	 * Retourne le hash encapsulé sous forme de chaine hexadécimale pour
	 * l'affichage.
	 */
	public String asHex() {
		StringBuffer sb = new StringBuffer();

		for(byte b : hash) {
			String h = String.format("%x", b);
			if(h.length() != 2) {
				sb.append("0");
			}
			sb.append(h);
		}

		return sb.toString();
	}
}
