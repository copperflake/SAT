package sat.utils.pftp;

import java.util.Arrays;

public class Hash {
	private byte[] hash;

	public Hash(byte[] hash) {
		this.hash = hash;
	}

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

	public int hashCode() {
		return Arrays.hashCode(hash);
	}

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
