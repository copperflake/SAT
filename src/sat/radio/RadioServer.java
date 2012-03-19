package sat.radio;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.PriorityQueue;

import sat.radio.engine.server.RadioServerEngine;
import sat.radio.engine.server.RadioServerEngineDelegate;
import sat.radio.message.Message;

/**
 * Un serveur radio.
 */
public class RadioServer extends Radio implements RadioServerEngineDelegate {
	private RadioServerDelegate delegate;
	private RadioServerEngine engine;
	
	private PriorityQueue<Message> incomingMessages;
	private PriorityQueue<Message> outgoingMessages;
	
	private Thread incomingThread;
	
	private HashMap<RadioID, RadioSocket> sockets;
	
	public RadioServer(RadioServerDelegate delegate) {
		setDelegate(delegate);
		this.sockets = new HashMap<RadioID, RadioSocket>();
		
		incomingMessages = new PriorityQueue<Message>();
		outgoingMessages = new PriorityQueue<Message>();
		
		incomingThread = new Thread() {
			public void run() {
				synchronized(incomingMessages) {
					while(true) {
						if(!incomingMessages.isEmpty()) {
							Message message = incomingMessages.poll();
							System.out.println("Got message " + message);
						} else {
							try {
								incomingMessages.wait();
							} catch(InterruptedException e) {
								// TODO handle
								e.printStackTrace();
							}
						}
					}
				}
			}
		};
		
		incomingThread.start();
	}
	
	public void setDelegate(RadioServerDelegate delegate) {
		this.delegate = delegate;
	}
	
	public void listen(RadioServerEngine engine) throws IOException {
		// TODO multiples engines !
		this.engine = engine;
		engine.init(this);
	}

	public void onNewConnection(RadioSocket socket) {
		new RadioSocketListener(socket).start();
	}
	
	protected class RadioSocketListener extends Thread {
		RadioSocket socket;
		
		public RadioSocketListener(RadioSocket socket) {
			this.socket = socket;
		}
		
		public void run() {
			try {
				ObjectInputStream ois = new ObjectInputStream(socket.in);
				System.out.println("Client Connected");
				
				Message message;
				while((message = (Message) ois.readObject()) != null) {
					synchronized(incomingMessages) {
						incomingMessages.add(message);
						incomingMessages.notify();
					}
				}
			} catch(Exception e) {
				// Close bad client
				e.printStackTrace();
				try {
					socket.close();
				} catch(IOException e1) {
					// TODO handle
					e1.printStackTrace();
				}
			}
		}
	}
}
