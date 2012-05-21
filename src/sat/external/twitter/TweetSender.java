package sat.external.twitter;

import java.io.File;

import sat.radio.RadioID;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TweetSender {
	private static Twitter twitter;

	private static void init() {
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

	public static void tweet(String tweet, File image, RadioID id) throws TwitterException {
		if(twitter == null) {
			init();
		}

		try {
			StatusUpdate status = new StatusUpdate(id.toString() + ": " + tweet);
			status.setMedia(image);
			twitter.updateStatus(status);
		}
		catch(TwitterException e) {
			e.printStackTrace();
		}
	}

	public static void tweet(String tweet) {
		if(twitter == null) {
			init();
		}

		try {
			twitter.updateStatus(tweet);
		}
		catch(TwitterException e) {
			e.printStackTrace();
		}
	}
}
