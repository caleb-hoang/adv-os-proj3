import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
// Client that requests to read or write an object upon being
// Arguments, in order:
// 0: Client number
// 1: Object
// 2: RequestType (1 for read, 0 for write)
// 3: Value

public class Client {

        public static int NUM_SERVERS = 7;
    public static void main(String[]args) throws IOException {
        if (args.length > 4 || args.length < 2) {
            System.out.println("Wrong number of args!");
            System.exit(0);
        }
        int clientNum = Integer.parseInt(args[0]);
        String objectName = args[1];
        int requestType = Integer.parseInt(args[2]);
        String objectValue = "";
        if (args.length == 4) {
            objectValue = args[3];
        }
        String[] ips = getIPs();
        int[] servers = relevantServers(objectName);

        if (requestType == 1) { // If reading
            read(clientNum, servers, ips, objectName);
        } else { // If writing
            Obj newObject = new Obj(objectName, objectValue);
            write(clientNum, servers, ips, newObject);
        }

        
    }

    public static String read(int clientNum, int[] servers, String[] ips, String objectName) throws IOException {
        Random rand = new Random();
        int chosenServer = servers[rand.nextInt(3)];
        int port = 7000 + chosenServer;
        System.out.println("Attempting to connect to server " + chosenServer +" on port" + port);
		Socket server = new Socket(InetAddress.getByName(ips[chosenServer]), port);
		System.out.println("Just connected to " + server.getRemoteSocketAddress());

		// Create output stream to server and send server the message
		OutputStream outToServer = server.getOutputStream();
		DataOutputStream out = new DataOutputStream(outToServer);
        InputStream inFromServer = server.getInputStream();
        DataInputStream in = new DataInputStream(inFromServer);
        out.writeUTF(clientNum + " " + 1 + " " + objectName);
        
        String result = in.readUTF();
        server.close();
        return result;
    }

    public static String write(int clientNum, int[] servers, String[] ips, Obj object) throws IOException {
        int numSuccesses = 0;
        for (int i = 0; i < servers.length; i++) {
            int currentServer = servers[i];
            int port = 7000 + currentServer;
            System.out.println("Attempting to connect to server " + currentServer +" on port" + port);
	    	Socket server = new Socket(InetAddress.getByName(ips[currentServer]), port);
    		System.out.println("Just connected to " + server.getRemoteSocketAddress());

	    	// Create output stream to server and send server the message
	    	OutputStream outToServer = server.getOutputStream();
	    	DataOutputStream out = new DataOutputStream(outToServer);
            InputStream inFromServer = server.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);
            out.writeUTF(clientNum + " " + 0 + " " + object.name + " " + object.value);
        
            String result = in.readUTF();
            if (result.equals("Successfully wrote!")) {
                numSuccesses ++;
                System.out.println("Send to server " + currentServer + " succeeded");
            } else {
                System.out.println("Send to server " + currentServer + " failed");
            }
            server.close();
        }
        if (numSuccesses >= 2) {
            return "Write successful!";
        } else {
            return "Write failed!";
        }
    }

    public static String[] getIPs() {
        Scanner file = new Scanner("ips.txt");
        String[] servers = new String[NUM_SERVERS];
        for(int i = 0; i < NUM_SERVERS; i++) {
            servers[i] = file.next();
        }
        file.close();
        return servers;
    }

    public static int[] relevantServers(String objectName) {
        int[] objectServers = new int[3];
        int hashCode = objectName.hashCode();
        objectServers[0] = (hashCode) % 7;
        objectServers[1] = (hashCode + 2) % 7;
        objectServers[1] = (hashCode + 4) % 7;
        return objectServers;
    }
}