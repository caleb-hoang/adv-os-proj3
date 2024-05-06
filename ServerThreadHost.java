import java.net.*;
import java.io.*;

public class ServerThreadHost extends ServerThread {
	// private int num
	// private int partner
	// private int port
	// private Socket socket
	// private DataOutputStream out
	// private DataInputStream in
	// private Server server
	// private boolean exit
	private ServerSocket serverSocket;
	
	public ServerThreadHost(int number, int oServer, int portNum, Server pServer) {
		exit = false;
		num = number;
		partner = oServer;
		port = portNum;
		server = pServer;
	}

	public void run() {
		try {
			//self
			if(server.serverID == partner)	{
				System.out.println("Thread " + num + " waiting for response from Server " + partner + " on port " + port);
				System.out.println("Thread " + num + " connected to Server " + partner);
				server.markReady();
				return;
			}

			System.out.println("Thread " + num + " waiting for response from Server " + partner + " on port " + port);
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			System.out.println("Thread " + num + " connected to Server " + partner);

			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			out.writeUTF("");
			in.readUTF();

			server.markReady();
			receiveMessages();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}