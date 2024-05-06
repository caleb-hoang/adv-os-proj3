import java.net.*;
import java.io.*;

public class ServerToClientThread {

    private int port;
	private int num;
	private ServerSocket serverSocket;
	private DataInputStream in;
	private DataOutputStream out;
	private Socket server;
    private Server parent;

    // For port, use 7000 + the Server's id

    public ServerToClientThread(int portNum, int threadNum, Server s) throws IOException {
		port = portNum;
		num = threadNum;
		serverSocket = new ServerSocket(port);
		parent = s;
	}

    public void run() {
        while(true) {
            try {
                System.out.println("Thread " + num + " waiting for client on port " + port);
                server = serverSocket.accept();

                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                in = new DataInputStream(server.getInputStream());

                String message = in.readUTF();
                out = new DataOutputStream(server.getOutputStream());
                String response = parent.getRequest(message);
                System.out.println("Attempting to respond with: " + response);
                out.writeUTF(response); // Print response from server.
            } catch (SocketTimeoutException s) {
                System.out.println("Socket Timeout on thread " + num);
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
}


}
