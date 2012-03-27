package sat.deprecated;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import sat.EndOfWorldException;
import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerEngineDelegate;

/**
 * Moteur d'échange de message par fichier.
 * <p>
 * En pratique ce moteur n'utilise pas de vrai fichier, mais des pipes *nix
 * créés avec `mkfifo`. Il n'est donc pas portable et peu performant.
 * <p>
 * Il a été développé pour respecter l'ordre des ITP du projet. Une fois les
 * moteurs réseau tel que RadioServerUDPEngine diponibles, ce moteur ne devrait
 * plus être utilisé.
 */
public class RadioServerFileEngine extends RadioServerEngine {
	private File fileIn, fileOut, lockFile;

	private FileInputStream fis, keepaliveOut;
	private FileOutputStream fos, keepaliveIn;

	private Thread reader;

	public RadioServerFileEngine(String path) {
		fileIn = new File(path + ".in");
		fileIn.deleteOnExit();

		fileOut = new File(path + ".out");
		fileOut.deleteOnExit();

		lockFile = new File(path + ".lock");
		lockFile.deleteOnExit();
	}

	public void init(RadioServerEngineDelegate delegate) throws IOException {
		setDelegate(delegate);

		// Crée le fichier de verrou
		new FileOutputStream(lockFile).close();

		// Création d'un fichier pipe FIFO
		boolean fifoCreationSuccess = true;
		try {
			// Suppression des fichiers fichiers d'entrée sorties s'ils
			// existent déjà.
			if((fileIn.exists() && !fileIn.delete()) || (fileOut.exists() && !fileIn.delete())) {
				throw new Exception();
			}

			// Cette fontionnalité n'est disponible que sur un système UNIX
			Process mkfifoIn = Runtime.getRuntime().exec("mkfifo " + fileIn.getPath());
			Process mkfifoOut = Runtime.getRuntime().exec("mkfifo " + fileOut.getPath());

			mkfifoIn.waitFor();
			mkfifoOut.waitFor();

			fifoCreationSuccess = fileIn.exists() && fileOut.exists();
		} catch(Exception e) {
			fifoCreationSuccess = false;
		}

		if(!fifoCreationSuccess) {
			throw new IOException("Unable to create pipe files");
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
					keepaliveIn = new FileOutputStream(fileIn, true);
					keepaliveOut = new FileInputStream(fileOut);
				} catch(FileNotFoundException e) {
					throw new EndOfWorldException("Unable to start pipe keep-alive");
				}
			}
		}).start();

		// Les flux d'entrée / sorties
		fis = new FileInputStream(fileIn);
		fos = new FileOutputStream(fileOut, true);

		// Lancement du thread de surveillance du pipe
		reader = new RadioServerFileEngineReader(this);
		reader.start();
	}

	private void receivedMessage(FileEngineMessage message) {
		// TODO Auto-generated method stub

	}

	private class RadioServerFileEngineReader extends Thread {
		RadioServerFileEngine engine;

		public RadioServerFileEngineReader(RadioServerFileEngine parent) {
			this.engine = parent;
		}

		public void run() {
			byte[] buffer = new byte[65535];

			int byteRead = 0;
			while(true) {
				try {
					byteRead = engine.fis.read(buffer);
				} catch(IOException e) {
					e.printStackTrace();
				}

				if(byteRead == -1) {
					throw new EndOfWorldException("Input pipe file reached EOF !");
				} else if(byteRead > 0) {
					ByteArrayInputStream objectBytes = new ByteArrayInputStream(buffer, 0, byteRead);

					try {
						ObjectInputStream ois = new ObjectInputStream(objectBytes);
						FileEngineMessage message = (FileEngineMessage) ois.readObject();

						engine.receivedMessage(message);
					} catch(Exception e) {
						// Silently drop invalid packet
					}
				}
			}
		}
	}
}
