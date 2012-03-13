package sat.file;

public class FileSegment {
	private byte[] data;
	
	public FileSegment(byte[] data) {
		this.data = data;
	}
	
	public byte[] getData() {
		return this.data;
	}
}
