package sat.external.twitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Twitter {
	//public Twitter() throws IOException {
	public static void main(String[] args) throws IOException {
		
		URL url = new URL("https://api.twitter.com/1/statuses/update.json?include_entities=true");
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);

		String method = "POST";
		
		String data = "Accept: */*\nConnection: close\nUser-Agent: OAuth gem v0.4.4\nContent-Type: application/x-www-form-urlencoded\nContent-Length: 76\nHost: api.twitter.com\n\nstatus=Hello%20Ladies%20%2b%20Gentlemen%2c%20a%20signed%20OAuth%20request%21";
		long timestamp = new Date().getTime()/1000;
		String sign = "";
		
		String consumerSecret = "H3Y5irP7FAgLhIPkYhR7q8xTCC16WWObwnjFPmsz2g";
		String tokenSecret = "qfXAM7XTG4IltakVgdwws5EBbDjY75zZA8ZkqOOo";
		
		data = "POST /1/statuses/update.json?include_entities=true HTTP/1.1\n";
		data += "Accept: *            /*\n";
		data += "Connection: close\n";
		data += "User-Agent: OAuth gem v0.4.4\n";
		data += "Content-Type: application/x-www-form-urlencoded\n";
		data += "Authorization: \n";
		data += "OAuth oauth_consumer_key=\"rG8eO3F3qejLGtKC52XG7Q\", \n";
		data += "oauth_nonce=\""+UUID.randomUUID().toString()+"\", \n";
		data += "oauth_signature=\""+sign+"\", \n";
		data += "oauth_signature_method=\"HMAC-SHA1\", \n";
		data += "oauth_timestamp=\""+timestamp+"\", \n";
		data += "oauth_token=\"68448337-IzktDjmTJwoXePsK1pbtTRs7BD6PTaWFj9lUhfaJU\", \n";
		data += "oauth_version=\"1.0\"\n";
		data += "Content-Length: 76\n";
		data += "Host: api.twitter.com\n";
		data += "status = Hello%20Ladies%20%2b%20Gentlemen%2c%20a%20signed%20OAuth%20request%21\n";
		
		
		String param_include =		 "include_entities=true";
		String param_OAConsumerKey = "OAuth oauth_consumer_key=rG8eO3F3qejLGtKC52XG7Q";
		String param_OANonce =		 "oauth_nonce="+UUID.randomUUID().toString();
		String param_OASignMethod =	 "oauth_signature_method=HMAC-SHA1";
		String param_OATimestamp =	 "oauth_timestamp="+timestamp;
		String param_OAToken =		 "oauth_token=68448337-IzktDjmTJwoXePsK1pbtTRs7BD6PTaWFj9lUhfaJU";
		String param_OAVersion =	 "oauth_version=1.0";
		String param_status =		 "status=Hello%20Ladies%20%2b%20Gentlemen%2c%20a%20signed%20OAuth%20request%21";

		String parameterString = param_include+"&";
		parameterString += param_OAConsumerKey+"&";
		parameterString += param_OANonce+"&";
		parameterString += param_OASignMethod+"&";
		parameterString += param_OATimestamp+"&";
		parameterString += param_OAToken+"&";
		parameterString += param_OAVersion+"&";
		parameterString += param_status;
		
		String signatureBaseString = method+"&"+"https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json";
		
		String signingKey = consumerSecret+"&"+tokenSecret;
		
		try {
			Mac mac;
			mac = Mac.getInstance("HmacSHA1");
			SecretKeySpec secret = new SecretKeySpec(signingKey.getBytes(),"HmacSHA1");
			mac.init(secret);
			byte[] binarySignature = mac.doFinal(signatureBaseString.getBytes());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
		out.write(data);
		out.close();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String decodedString;
		while ((decodedString = in.readLine()) != null) {
			System.out.println(decodedString);
		}
		in.close();
	}
}
