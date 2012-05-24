package sat.utils.base64;

// Note: pas utilisée dans notre projet, mais toujours utile à avoir sous la main!

/**
 * Encodage / Décodage en Base64.
 */
public class Base64 {
	/**
	 * L'alphabet d'encodage
	 */
	private static byte[] alpha = {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
		'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b',
		'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
		'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3',
		'4', '5', '6', '7', '8', '9', '-', '/', '=' };

	/**
	 * Encode une suite de bytes quelconques en Base64.
	 * 
	 * @param raw
	 *            Les données sources à encoder.
	 * @return Les données encodées.
	 */
	public static byte[] encode(byte[] raw) {
		int encoded_units = raw.length / 3 + ((raw.length % 3 != 0) ? 1 : 0);
		byte[] encoded = new byte[encoded_units * 4];

		byte A, B, C;		// Original bytes
		byte a, b, c, d;	// Encoded bytes
		byte padding = 0;

		for(int i = 0, j = 0; i < raw.length; i += 3, j += 4) {
			A = raw[i];

			if(i + 1 < raw.length) {
				B = raw[i + 1];
				if(i + 2 < raw.length) {
					C = raw[i + 2];
				}
				else {
					C = 0;
					padding = 1;
				}
			}
			else {
				B = 0;
				C = 0;
				padding = 2;
			}

			a = (byte) ((A & 0xFC) >> 2);
			b = (byte) (((A & 0x3) << 4) + ((B & 0xF0) >> 4));
			c = (byte) (((B & 0xF) << 2) + ((C & 0xC0) >> 6));
			d = (byte) (C & 0x3F);

			encoded[j] = alpha[a];
			encoded[j + 1] = alpha[b];
			encoded[j + 2] = padding > 1 ? alpha[64] : alpha[c];
			encoded[j + 3] = padding > 0 ? alpha[64] : alpha[d];
		}

		return encoded;
	}

	/**
	 * Décode une suite de bytes en Base64 en byte quelconques.
	 * 
	 * @param encoded
	 *            Les données encodées.
	 * @return Les données sources décodées ou un buffer vide si les données
	 *         sont invalides.
	 */
	public static byte[] decode(byte[] encoded) {
		// Invalid data
		if(encoded.length % 4 != 0)
			return new byte[0];

		// Pre-compute the padding for buffer allocation
		byte padding = 0;
		for(int i = encoded.length - 1; i > encoded.length - 3; i--) {
			if(encoded[i] == alpha[64])
				padding++;
		}

		// Each unit of 4 bytes is computed in one cycle.
		int raw_units = (encoded.length / 4);
		byte[] raw = new byte[raw_units * 3 - padding];

		byte A, B, C;		// Original bytes
		byte a, b, c, d;	// Encoded bytes
		padding = 0;		// Reset padding

		// Compute each encoded unit one by one
		for(int i = 0, j = 0; i < encoded.length; i += 4, j += 3) {
			// Try to decode all four encoded chars
			try {
				a = decodeChar(encoded[i]);
				b = decodeChar(encoded[i + 1]);
				c = decodeChar(encoded[i + 2]);
				d = decodeChar(encoded[i + 3]);
			}
			catch(Exception e) {
				// Invalid data
				return new byte[0];
			}

			// Padding
			if(c == 64) {
				c = 0;
				padding++;
			}

			if(d == 64) {
				d = 0;
				padding++;
			}

			// Restore original bytes
			A = (byte) ((a << 2) + ((b & 0x30) >> 4));
			B = (byte) (((b & 0xf) << 4) + ((c & 0x3C) >> 2));
			C = (byte) (((c & 0x3) << 6) + d);

			// Copy to buffer
			raw[j] = A;
			if(padding < 2)
				raw[j + 1] = B;
			if(padding < 1)
				raw[j + 2] = C;
		}

		return raw;
	}

	/**
	 * Décode un caractère encodé en Base64.
	 */
	private static byte decodeChar(byte character) throws Exception {
		for(byte i = 0; i < alpha.length; i++) {
			if(alpha[i] == character)
				return i;
		}

		throw new Exception("Invalid character");
	}
}
