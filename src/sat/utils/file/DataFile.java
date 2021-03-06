package sat.utils.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Un fichier segmentable pour l'envoi par la radio.
 */
@SuppressWarnings("serial")
public class DataFile extends File implements Iterable<byte[]> {
	/**
	 * Nombre de partie à ce fichier.
	 */
	private int segmentsCount = 0;

	/**
	 * Le flux d'entrée du fichier.
	 */
	private RandomAccessFile file;

	/**
	 * La taille maximale d'un segment de fichier.
	 */
	public static final int SEGMENT_SIZE = 1024;

	public DataFile(String path) throws IOException, NoSuchAlgorithmException {
		super(path);
		file = new RandomAccessFile(this, "rw");
		segmentsCount = segmentsCountForSize((int) file.length());
	}

	/**
	 * Retourne un itéreateur pour parcourir ce fichier morceau par morceau.
	 */
	public Iterator<byte[]> iterator() {
		return new Iterator<byte[]>() {
			/**
			 * La position actuelle dans le fichier.
			 */
			private int currentSegment = 0;

			/**
			 * Indique s'il y a un prochain morceau.
			 */
			public boolean hasNext() {
				return currentSegment < segmentsCount;
			}

			/**
			 * Retourne le prochain morceau de ce fichier.
			 */
			public byte[] next() {
				try {
					return getSegment(currentSegment++);
				}
				catch(IOException e) {
					e.printStackTrace();
					return null;
				}
			}

			public void remove() {
				// Not available !
			}
		};
	}

	/**
	 * Retourne un segment précis du fichier.
	 */
	public byte[] getSegment(int offset) throws IOException {
		byte[] bytes = new byte[SEGMENT_SIZE];

		file.seek(offset * SEGMENT_SIZE);
		int bytesRead = file.read(bytes, 0, SEGMENT_SIZE);

		// Truncate array if we havent read a full block of data
		if(bytesRead < SEGMENT_SIZE) {
			bytes = Arrays.copyOfRange(bytes, 0, bytesRead);
		}

		return bytes;
	}

	/**
	 * Ecrit un segment donné à une position précise.
	 */
	public void writeSegment(int offset, byte[] data) throws IOException {
		file.seek(offset * SEGMENT_SIZE);
		file.write(data);
	}

	/**
	 * Calcul le hash du fichier. Si cette méthode est appelée sur un fichier en
	 * cours de reconstitution, son résultat sera bien entendu erroné (par
	 * rapport à la version complète du même fichier).
	 */
	public byte[] getHash() throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("SHA1");

		file.seek(0);

		byte[] buffer = new byte[1024];
		int numRead;

		do {
			numRead = file.read(buffer);
			if(numRead > 0) {
				digest.update(buffer, 0, numRead);
			}
		}
		while(numRead != -1);

		return digest.digest();
	}

	/**
	 * Retourne l'extension du fichier.
	 */
	public String getFormat() {
		String name = getName();
		int dotOffset = -1;

		for(int i = name.length() - 1; i >= 0; i--) {
			if(name.charAt(i) == '.') {
				dotOffset = i;
				break;
			}
		}

		if(dotOffset == -1)
			return "undefined";
		else {
			String format = "";
			for(int i = dotOffset + 1; i < name.length(); i++)
				format += name.charAt(i);
			return format;
		}
	}

	/**
	 * Retourne la taille déclarée de ce fichier.
	 */
	public int getSize() throws IOException {
		return (int) this.file.length();
	}

	/**
	 * Ferme ce fichier.
	 */
	public void close() throws IOException {
		file.close();
	}

	/**
	 * Calcul le nombre de segments nécessaires pour un taille de fichier donnée
	 * (en byte).
	 */
	public static int segmentsCountForSize(int size) {
		return (int) (Math.ceil(((float) size) / ((float) DataFile.SEGMENT_SIZE)));
	}
}
