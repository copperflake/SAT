package sat.radio.message.handler;

import sat.radio.RadioProtocolException;
import sat.radio.message.*;

public interface MessageHandler {
	public void handle(MessageBye message) throws RadioProtocolException;
	public void handle(MessageChoke message) throws RadioProtocolException;
	public void handle(MessageData message) throws RadioProtocolException;
	public void handle(MessageHello message) throws RadioProtocolException;
	public void handle(MessageKeepalive message) throws RadioProtocolException;
	public void handle(MessageLanding message) throws RadioProtocolException;
	public void handle(MessageMayDay message) throws RadioProtocolException;
	public void handle(MessageRouting message) throws RadioProtocolException;
	public void handle(MessageSendRSAKey message) throws RadioProtocolException;
	public void handle(MessageUnchoke message) throws RadioProtocolException;
}
