package sat.tower.agent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import sat.events.Event;
import sat.events.EventListener;

public class AgentServer implements Runnable {
	private static final int PORT = 4242;
	private static Thread serverThread = null;
	private static boolean running = false;

	public void run() {
		ServerSocket server = null;

		try {
			running = true;

			// No security built-in, so listen only on localhost
			InetAddress iface = InetAddress.getByName("localhost");
			server = new ServerSocket(PORT, 50, iface);

			while(running) {
				Socket client = server.accept();
				SocketManager manager = new SocketManager(client);
				manager.start();
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				if(server != null) {
					server.close();
				}
			}
			catch(IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isRunning() {
		return (serverThread != null && serverThread.isAlive());
	}

	public static void start() {
		if(isRunning()) {
			return;
		}

		serverThread = new Thread(new AgentServer());
		serverThread.start();
	}

	public static void stop() {
		running = false;
	}

	// - - - Socket Manager - - -

	public class SocketManager extends Thread implements EventListener {
		private Socket socket;

		private ObjectOutputStream oos;
		private ObjectInputStream ois;

		private TowerAgent agent;

		public SocketManager(Socket socket) throws IOException {
			this.socket = socket;

			agent = new TowerAgent();
			agent.addListener(this);
		}

		public void run() {
			try {
				oos = new ObjectOutputStream(socket.getOutputStream());
				ois = new ObjectInputStream(socket.getInputStream());

				while(true) {
					AgentRequest req = (AgentRequest) ois.readObject();
					agent.execute(req);
				}
			}
			catch(Exception e) {
			}

			exit();
		}

		public void exit() {
			agent.exit();

			try {
				socket.close();
			}
			catch(Exception e) {
			}

			this.interrupt();
		}

		public void on(Event event) {
			try {
				oos.writeObject(event);
				oos.flush();
			}
			catch(IOException e) {
				e.printStackTrace();
				exit();
			}
		}
	}
}
