package sat.tower;

import java.util.PriorityQueue;

import sat.radio.message.Message;

public class Journal {
	PriorityQueue<Message> queue = new PriorityQueue<Message>(1000);
}
