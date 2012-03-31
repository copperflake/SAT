package sat.utils.crypto;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Un flux de sortie ajoutant le support de l'encryption RSA à un autre flux.
 */
public class RSAOutputStream extends OutputStream {
	/**
	 * Le flux de sortie dans lequel seront écrit les données encryptées.
	 */
	protected OutputStream out;

	/**
	 * La paire de clés utilisée pour l'encryption RSA. Seul la composante
	 * publique de la paire de clé sera utilisée.
	 */
	protected RSAKeyPair keys;

	/**
	 * La taille des blocs de données. Elle est déterminée en fonction de la
	 * taille des clés utilisées.
	 */
	protected int blockSize;

	/**
	 * Un buffer qui contiendra les données à crypter et cryptées.
	 */
	protected byte[] block;

	/**
	 * La taille du buffer de données utilisateur. Le buffer est égale à la
	 * taille des blocs moins 4.
	 */
	protected int bufferSize;

	/**
	 * La longueur actuelle effective du buffer utilisateur.
	 */
	protected int bufferLength = 0;

	/**
	 * Le buffer de données utilisateur.
	 */
	protected byte[] buffer;

	/**
	 * Un générateur de nombre aléatoire cryptographiquement sûrs. Il sera
	 * initialisé lorsqu'un padding sera nécessaire pour compléter le bloc.
	 */
	protected SecureRandom rand = null;

	/**
	 * Crée une flux de cryptage RSA.
	 * 
	 * @param out
	 *            Le flux interne dans lequel seront écrites les données
	 *            cryptées.
	 * @param keys
	 *            La paire de clé utilisée pour le cryptage RSA.
	 */
	public RSAOutputStream(OutputStream out, RSAKeyPair keys) {
		this.out = out;
		this.keys = keys;

		rand = new SecureRandom();

		blockSize = (keys.keyLength() / 8) + 1;
		bufferSize = blockSize - 4; // Front byte + Padding boundary

		buffer = new byte[bufferSize];

		// Only one block allocation (no zero-ing memory on each flush)
		block = new byte[blockSize];
	}

	/**
	 * Écrit un byte de donnée en clair dans le flux RSA.
	 * <p>
	 * Les données seront misent en mémoire tampon jusqu'à ce qu'à ce que
	 * suffisement de données soient accumulée pour former unbloc de donnée
	 * complet, après quoi, le tampon sera automatiquement vidé.
	 * 
	 * @param b
	 *            Un byte de donnée à crypter. Les bits de poids fort dépassant
	 *            la capacité du byte seront ignorés.
	 */
	public void write(int b) throws IOException {
		buffer[bufferLength++] = (byte) (b & 0xff);

		// Quand le tampon de donnée est complet, on envoi automatiquement
		// les données accumulées.
		if(bufferLength >= bufferSize)
			flush();
	}

	/**
	 * Vide le tampon du flux et envoie un paquet de données.
	 * <p>
	 * Les données manquantes pour compléter un bloc de données seront
	 * remplacées par du padding aléatoire avant d'être encryptées.
	 * <p>
	 * Si le buffer utilisateur est vide, cette méthode de fait rien.
	 */
	public void flush() throws IOException {
		// Nothing to send, so we don't send anything.
		if(bufferLength == 0)
			return;

		// Front zero
		// BigInt(block) < (key-modulo - 1)
		block[0] = 0;
		block[1] = 0;

		// Padding
		int padding = (blockSize - 3) - bufferLength;

		byte[] padding_bytes = new byte[padding];
		rand.nextBytes(padding_bytes);

		for(int i = 0; i < padding_bytes.length; i++) {
			// Check zero-byte
			while(padding_bytes[i] == 0) {
				padding_bytes[i] = (byte) (rand.nextInt() & 0xff);
			}
		}

		System.arraycopy(padding_bytes, 0, block, 2, padding);

		// Padding boundary
		block[padding + 2] = 0;

		// Buffer copying
		System.arraycopy(buffer, 0, block, padding + 3, bufferLength);

		// Encrypt
		byte[] block_encrypted = keys.encrypt(new BigInteger(block)).toByteArray();

		padding = block.length - block_encrypted.length;

		for(int i = 0; i < padding; i++)
			block[i] = 0;

		System.arraycopy(block_encrypted, 0, block, padding, block_encrypted.length);

		out.write(block);
		out.flush();

		bufferLength = 0;
	}

	/**
	 * Ferme le flux de cryptage RSA et vide le tampon de sortie.
	 */
	public void close() throws IOException {
		flush(); // If buffer isn't empty, flush it
		out.close();
	}
}
