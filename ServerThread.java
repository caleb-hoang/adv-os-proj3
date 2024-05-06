import java.net.*;
import java.io.*;
import java.util.*;

// Thread which accepts communication from other Servers through their ServerThreads.
public abstract class ServerThread extends Thread {

	protected int num;
	protected int partner;
	protected int port;
	protected Socket socket;
	protected DataOutputStream out;
	protected DataInputStream in;
	protected Server server;	
	protected boolean exit;
	protected Random rand = new Random();

	// Determines whether a received message is delayed before being processed
	boolean receiveDelay = true;
	
	public int getNum() {
		return this.num;
	}

	// Sends message to partner and prints the message to console.
	public boolean sendMessage(Message message) throws IOException {
		out.writeUTF(message.toString());
		String response = in.readUTF();
		if (response.equals("Delivered")) {
			// TODO: Message was received by partner, and the channel is working properly.
			System.out.println("Message received");
			return true;
		} else {
			// TODO: Message was not received by partner, and the channel is not working properly.
			// You may want to add behavior here to respond to blocked channels.
			System.out.println("Message not received");
			return false;
		}
	}

	// Indicate that the thread will not be sending a message this round.
	public void pass() { 
		try {
			out.writeUTF("pass");
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	// Listens for message from the partner, then prints to console when received.
	public void receiveMessages() {
		while(!exit) {
			try {
				System.out.println("listening for messages from server " + partner);
				String nextMessage = in.readUTF();
				if (!server.isChannelClosed(partner)) {
					System.out.println("Message " + nextMessage + " received from server " + partner);
					out.writeUTF("Delivered");
					server.receiveMessage(nextMessage);
				} else {
					System.out.println("Message " + nextMessage + " blocked from server " + partner);
					out.writeUTF("Failed");
				}
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
	}
	
	// Signal the socket to close.
	public void stopSig() {
		System.out.println("Disconnected from server " + partner);
		exit = true;
		server.unmarkReady(num);
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
