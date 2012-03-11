package sat.radio.engine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import sat.EndOfWorldException;
import sat.radio.RadioServer;

public class RadioServerFileEngine extends RadioServerEngine {
	private File file, lockFile;
	private Thread reader;
	
	private FileInputStream fis;
	FileOutputStream keepaliveStream;
	
	public RadioServerFileEngine(String path) {
		file = new File(path);
		file.deleteOnExit();
		
		lockFile = new File(path + ".lock");
		lockFile.deleteOnExit();
	}
	
	public void init(RadioServer radioServer) throws IOException {
		super.init(radioServer);
		
		// Crée le fichier de verrou
		new FileOutputStream(lockFile).close();
		
		// Création d'un fichier pipe FIFO
		boolean fifoCreationSuccess = true;
		try {
			// Cette fontionnalité n'est disponible que sur un système UNIX
			Runtime.getRuntime().exec("mkfifo " + file.getAbsolutePath()).waitFor();
			fifoCreationSuccess = file.exists();
		} catch(Exception e) {
			fifoCreationSuccess = false;
		}
		
		if(!fifoCreationSuccess) {
			throw new IOException("Unable to create pipe file");
		}
	
		// Les fichiers pipes envoyent EOF quand le dernier client se
		// déconnecte. Comme celui-ci ne se déconnectera jamais, il n'y
		// aura jamais de EOF !
		//
		// De plus, l'ouverture d'un pipe est bloquante tant qu'une autre
		// entité n'est pas connectée de l'autre côté. Il faut donc ouvrir
		// le pipe des deux côtés en même temps, donc utiliser un thread !
		(new Thread() {
			public void run() {
				try {
					keepaliveStream = new FileOutputStream(file, true);
				} catch (FileNotFoundException e) {
					throw new EndOfWorldException("Unable to start pipe keep-alive");
				}
			}
		}).start();
		
		// Le flux d'entrée
		fis = new FileInputStream(file);
		
		// Lancement du thread de surveillance du pipe
		reader = new RadioServerFileEngineReader(this);
		reader.start();
	}
	
	private class RadioServerFileEngineReader extends Thread {
		RadioServerFileEngine engine;
		
		public RadioServerFileEngineReader(RadioServerFileEngine parent) {
			this.engine = parent;
		}
		
		public void run() {
			byte[] buffer = new byte[1024];
			
			int byteRead = 0;
			while(true) {
				try {
					byteRead = engine.fis.read(buffer);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(byteRead == -1) {
					throw new EndOfWorldException("Input pipe file reached EOF !");
				} else {
					System.out.println(byteRead);
					for(byte b : buffer)
						System.out.print((char)b);
					System.out.println();
				}
			}
		}
	}
}
