package sat.radio;

import sat.radio.message.Message;
import sat.utils.cli.Config;
import sat.utils.geo.Coordinates;

public interface RadioDelegate {
	public Config getConfig();
	public Coordinates getLocation();
	public void onMessage(Message message);
}
