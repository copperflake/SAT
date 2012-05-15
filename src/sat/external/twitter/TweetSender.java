package sat.external.twitter;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

public class TweetSender {
	public TweetSender(String tweet) {
		String consumerKey = "rG8eO3F3qejLGtKC52XG7Q";
		String consumerSecret = "H3Y5irP7FAgLhIPkYhR7q8xTCC16WWObwnjFPmsz2g";
		String accessToken = "68448337-IzktDjmTJwoXePsK1pbtTRs7BD6PTaWFj9lUhfaJU";
		String accessTokenSecret = "qfXAM7XTG4IltakVgdwws5EBbDjY75zZA8ZkqOOo";
			
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
			.setOAuthConsumerKey(consumerKey)
			.setOAuthConsumerSecret(consumerSecret)
			.setOAuthAccessToken(accessToken)
			.setOAuthAccessTokenSecret(accessTokenSecret);
			
		TwitterFactory tf = new TwitterFactory(cb.build());
		//AccessToken accessTokens = new AccessToken(accessToken, accessTokenSecret); 
		Twitter twitter = tf.getInstance();
		   
	    /*RequestToken twitterRequestToken = twitter.getOAuthRequestToken();
		String token = twitterRequestToken.getToken();
		String tokenSecret = twitterRequestToken.getTokenSecret();
		persist(token, tokenSecret);*/
		
		Status status;
		try {
			status = twitter.updateStatus(tweet);
			System.out.println("Successfully updated the status to [" + status.getText() + "].");
		}
		catch(TwitterException e) {
		// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	public static void main(String[] args) throws TwitterException {
		new TweetSender("Test");
	}
}
