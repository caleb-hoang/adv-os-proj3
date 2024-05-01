import java.net.*;
import java.io.*;
// Client that requests to read or write an object upon being
// Arguments, in order:
// 0: IP of Server
// 1: Port to connect through
// 2: Client number
// 3: Object
// 4: RequestType (1 for read, 0 for write)
// 5: Message
public class Client {
    public static void main(String[]args) throws IOException {
        if (args.length != 4) {
            System.out.println("Wrong number of args!");
            System.exit(0);
        }
        int port = (8000 + Integer.parseInt(args[1]));
        System.out.println("Attempting to connect to " + args[0] + " on port " + port);
		Socket server = new Socket(InetAddress.getByName(args[0]), port);
		System.out.println("Just connected to " + server.getRemoteSocketAddress());
		// Create output stream to server and send server the message
		OutputStream outToCoordinator = server.getOutputStream();
		DataOutputStream out = new DataOutputStream(outToCoordinator);
        if (Integer.parseInt(args[4]) == 1) { // If read request
            out.writeUTF(args[2] + " " + args[3] + " " + args[4]);
        } else { // If write request
            out.writeUTF(args[2] + " " + args[3] + " " + args[4] + " " + args[5]);
        }
		
			
		// Create input stream from coordinator and listens until the IP addresses are all obtained
		InputStream inFromCoordinator = server.getInputStream();
		DataInputStream in = new DataInputStream(inFromCoordinator);
        System.out.println(in.readUTF());
        server.close();
    }
}
