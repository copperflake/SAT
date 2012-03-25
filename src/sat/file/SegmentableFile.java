package sat.file;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

public class SegmentableFile implements Iterable<FileSegment> {
	private int segmentsCount = 0;
	private String pathname;
	private RandomAccessFile file;
	private byte[] hash;
	public static final int SEGMENT_SIZE = 1024;

	public SegmentableFile(String pathname) throws IOException {
		this.pathname = pathname;
		this.file = new RandomAccessFile(this.pathname, "rw");
		try {
			this.calculateHash();
		} catch(NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Iterator<FileSegment> iterator() {
		return new Iterator<FileSegment>() {
			private int currentSegment = 0;

			public boolean hasNext() {
				return currentSegment <= segmentsCount;
			}

			public FileSegment next() {
				try {
					return getSegment(currentSegment++);
				} catch(IOException e) {
					e.printStackTrace();
					// TODO blah
					return null;
				}
			}

			public void remove() {
				// Not available !
			}

			public int getCounter() {
				return currentSegment;
			}
		};
	}

	public FileSegment getSegment(int offset) throws IOException {
		byte[] bytes = null;
		file.read(bytes, offset * SEGMENT_SIZE, SEGMENT_SIZE);
		return new FileSegment(bytes);
	}

	public void writeSegment(int offset, FileSegment data) throws IOException {
		file.write(data.getData(), offset * SEGMENT_SIZE, SEGMENT_SIZE);
	}

	public byte[] calculateHash() throws NoSuchAlgorithmException, IOException {
		MessageDigest digest = MessageDigest.getInstance("MD5");

		byte[] buffer = new byte[1024];
		int numRead;

		do {
			numRead = file.read(buffer);
			if(numRead > 0) {
				digest.update(buffer, 0, numRead);
			}
		} while(numRead != -1);

		return digest.digest();
	}

	public byte[] getHash() {
		return this.hash;
	}

	public String getFormat() {
		String name = this.pathname;
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

	public long getSize() throws IOException {
		return this.file.length();
	}
}
