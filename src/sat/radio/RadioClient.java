package sat.radio;

import java.io.IOException;

import sat.plane.Route;
import sat.radio.engine.client.RadioClientEngine;
import sat.radio.engine.client.RadioClientEngineDelegate;
import sat.radio.message.MessageLanding;
import sat.radio.message.MessageMayDay;
import sat.radio.message.MessageRouting;

public class RadioClient extends Radio implements RadioClientEngineDelegate {
	RadioClientDelegate delegate;
	
	public RadioClient(RadioClientDelegate delegate) {
		this.delegate = delegate;
	}
	
	public void connect(RadioClientEngine engine) throws IOException {
		engine.init(this);
	}
	
	public void mayDay(String cause, String dest) {
		this.send(new MessageMayDay(cause), dest);
	}
	
	public void landing(String dest) {
		this.send(new MessageLanding(), dest);
	}
	
	public void routing(Route route, String dest) {
		this.send(new MessageRouting(route), dest);
	}
	
	/*public void connect(InetAddress IP) {
		
	}*/
}
