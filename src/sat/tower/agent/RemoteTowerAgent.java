package sat.tower.agent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import sat.EndOfWorldException;
import sat.events.Event;

public class RemoteTowerAgent extends TowerAgent {
	private ObjectOutputStream oos;
	private ObjectInputStream ois;

	public RemoteTowerAgent() throws UnknownHostException, IOException {
		Socket socket = new Socket(InetAddress.getByName("localhost"), 4242);

		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());

		(new Thread() {
			public void run() {
				try {
					while(true) {
						Event event = (Event) ois.readObject();
						emit(event);
					}
				}
				catch(Exception e) {
					e.printStackTrace();
					throw new EndOfWorldException("Error reading remote agent stream");
				}
			}
		}).start();
	}

	public void execute(AgentRequest req) {
		try {
			oos.writeObject(req);
		}
		catch(IOException e) {
			e.printStackTrace();
			throw new EndOfWorldException("Error writing remote agent stream");
		}
	}
}
