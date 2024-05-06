import java.net.*;
import java.io.*;

public class ServerThreadClient extends ServerThread {
	// private int num
	// private int partner
	// private int port
	// private Socket socket
	// private DataOutputStream out
	// private DataInputStream in
	// private Server server
	// private boolean exit
	private String ip;
	
	public ServerThreadClient(int number, int host, int portNum, Server pServer, String address) {
		exit = false;
		num = number; 
		partner = host;
		port = portNum;
		server = pServer;
		ip = address;
	}
	
	public void run() {
		try {

			//to allow all serverhost threads to be ready
			try {
				Thread.sleep(5000);
			} catch (InterruptedException ie) {
				System.out.println("Can't sleep a thread!");
			}

			System.out.println("Thread " + num + " attempting to connect to " + ip + " on port " + port);
			while(true) {
				try {
					socket = new Socket(InetAddress.getByName(ip), port);
					break;
				} catch (ConnectException c) {
					c.printStackTrace();
				}
			}
			System.out.println("Thread " + num + " connected to Server " + partner);

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("");
			in.readUTF();

			server.markReady();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
