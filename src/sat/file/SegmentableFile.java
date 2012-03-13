package sat.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;

public class SegmentableFile implements Iterable<FileSegment> {
	private int segmentsCount = 0;
	private String pathname;
	private RandomAccessFile file;
	public static final int SEGMENT_SIZE = 1024;
	
	public SegmentableFile(String pathname) throws FileNotFoundException {
		this.pathname = pathname;
		this.file = new RandomAccessFile(this.pathname, "rw");
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
				} catch (IOException e) {
					e.printStackTrace();
					// TODO blah
					return null;
				}
			}

			public void remove() {
				// Not available !
			}
		};
	}
	
	public FileSegment getSegment(int offset) throws IOException {
		byte[] bytes = null;
		file.read(bytes, offset*SEGMENT_SIZE, SEGMENT_SIZE);
		return new FileSegment(bytes);
	}
	
	public void writeSegment(int offset, FileSegment data) throws IOException {
		file.write(data.getData(), offset*SEGMENT_SIZE, SEGMENT_SIZE);
	}
}
