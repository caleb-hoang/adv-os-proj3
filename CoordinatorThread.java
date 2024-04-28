import java.net.*;
import java.io.*;


// Thread that communicates with client, sends IP to Coordinator, then sends compiled list of ips to the client.
public class CoordinatorThread extends Thread {
	private int port;
	private int num;
	private ServerSocket serverSocket;
	private ManagerThread manager;
	private DataInputStream in;
	private DataOutputStream out;
	private Socket server;

	public CoordinatorThread(int portNum, int threadNum, ManagerThread tManager) throws IOException {
		port = portNum;
		num = threadNum;
		serverSocket = new ServerSocket(port);
		manager = tManager;
	}

	public void run() {
			try {
				System.out.println("Thread " + num + " waiting for client on port " + port);
				server = serverSocket.accept();

				System.out.println("Just connected to " + server.getRemoteSocketAddress());
				in = new DataInputStream(server.getInputStream());
				
				
				String newIP = in.readUTF();
				
				System.out.println("Server " + num + " ip: " + newIP);
				synchronized(manager) {	
					int numWaiting = Coordinator.logIP(newIP, num);
					if (numWaiting != 0) {
						manager.wait();
					} else {
						manager.notifyAll();
					}
				}
				System.out.println("Thread " + num + " sending IPs!");
				String output = makeOutput(Coordinator.getIPs());
				out = new DataOutputStream(server.getOutputStream());
				out.writeUTF(output);
				Coordinator.isReady();
			} catch (SocketTimeoutException s) {
				System.out.println("Socket Timeout on thread " + num);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.out.println("Interrupted on thread " + num);
			}
	}
	
	// Converts array to string that can be sent as a message.
	public String makeOutput(String[] input) {
		String output = "";
		for (String next : input) {
			output = output + " " + next;
		}
		return output;
	}
}
