package sat.external.twitter;

import java.io.File;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TweetSender {
	private Twitter twitter;

	private void init() {
		String consumerKey = "PgIak9tCFu3ZP7VjN1pRCQ";
		String consumerSecret = "Dktte6oKunqQcF5fcYYcTzEqayC3oxt3kE4xVFQHY";
		String accessToken = "582857933-QJRDlFQfyGswckTczz7Qfopi00MQrXyLuifrEkOV";
		String accessTokenSecret = "GL0wMnXRFkoeN131VyLoyTpNstPp1BUiCEfhwL1VI";

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKey)
			.setOAuthConsumerSecret(consumerSecret)
			.setOAuthAccessToken(accessToken)
			.setOAuthAccessTokenSecret(accessTokenSecret)
			.setDebugEnabled(true);

		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();
	}
	
	protected void publish(String tweet, File image, byte[] PlaneID) throws TwitterException {
		init();
		try {
			StatusUpdate status = new StatusUpdate(new String(PlaneID)+": "+ tweet);
			status.setMedia(image);
			twitter.updateStatus(status);
		}
		catch(TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public TweetSender(String tweet) {
		init();
		try {
			twitter.updateStatus(tweet);
		}
		catch(TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
