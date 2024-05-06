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

    public static int NUM_SERVERS = 2;
    public static int NUM_REPLICATION = 3;

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
            String result = read(clientNum, servers, ips, objectName);
            System.out.println("Result of read: " + result);
        } else { // If writing
            Obj newObject = new Obj(objectName, objectValue);
            write(clientNum, servers, ips, newObject);
        }
    }

    public static String read(int clientNum, int[] servers, String[] ips, String objectName) throws IOException {
        Random rand = new Random();
        int chosenServerIndex = servers[rand.nextInt(NUM_REPLICATION)];
        int port = 7000 + 100*chosenServerIndex + chosenServerIndex;

        System.out.println("Attempting to connect to server " + chosenServerIndex + " on port " + port);
		Socket server = new Socket(InetAddress.getByName(ips[chosenServerIndex]), port);
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
            int chosenServerIndex = servers[i];
            int port = 7000 + 100*chosenServerIndex + chosenServerIndex;

            System.out.println("Attempting to connect to server " + chosenServerIndex +" on port " + port);
	    	Socket server = new Socket(InetAddress.getByName(ips[chosenServerIndex]), port);
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
                System.out.println("Send to server " + chosenServerIndex + " succeeded");
            } else {
                System.out.println("Send to server " + chosenServerIndex + " failed");
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
        String[] servers = new String[NUM_SERVERS];

        try {
            File file = new File("ips.txt");
            Scanner input = new Scanner(file);

            for(int i = 0; i < NUM_SERVERS; i++) {
                if(input.hasNextLine())  servers[i] = input.nextLine().toString();
            }

            input.close();
        } catch (FileNotFoundException f) {
            f.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return servers;
    }

    public static int[] relevantServers(String objectName) {
        int[] objectServers = new int[NUM_REPLICATION];
        int hashCode = objectName.hashCode();

        objectServers[0] = (hashCode) % NUM_SERVERS;
        objectServers[1] = (hashCode + 2) % NUM_SERVERS;
        objectServers[2] = (hashCode + 4) % NUM_SERVERS;

        return objectServers;
    }
}