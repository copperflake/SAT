package sat.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Scanner;

public class DataFile extends File {
	private byte[] hash;
	private byte[] receivedData;
	private int packetSize;
	private int lastPacketSize;
	private int numberOfPackets;
	
	public DataFile(String path) {
		super(path);
		BufferedInputStream stream = new BufferedInputStream(in)
		this.packetSize = stream.read(this.receivedData);
		this.hash = this.computeHash();
	}
	
	public void save(String name) throws IOException {
		RandomAccessFile file = new RandomAccessFile(name, "rw");
		file.write(receivedData);
	}
	
	public boolean isComplete() {
		return false;
	}
	
	private byte[] computeHash(byte[] file) throws NoSuchAlgorithmException {
		MessageDigest hasher = MessageDigest.getInstance("SHA-1");
		
		Formatter formatter = new Formatter();
		
		for(byte i : hasher.digest(file)) {
	        formatter.format("%02x", i);
		}
		
		return formatter.toString().getBytes();
	}
	
	public byte[] getBlock(int position) throws FileNotFoundException {
		Scanner scan = new Scanner(this);
	}
}
