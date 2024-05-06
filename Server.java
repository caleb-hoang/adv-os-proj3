import java.net.*;
import java.io.*;
import java.util.*;
// Server program which first obtains the IPs of all other ports through the coordinator,
// Then creates threads to handle communications between each of the servers.
// Then manages the delivery of each message.

// args[0] should be the IP of the Coordinator, and args[1] should be the index of the server (0-6).
public class Server {
	public final int NUM_SERVERS = 5; // -> 7
    public final int BASE_PORT = 7000;
	public final int COORDINATOR_BASE_PORT = 8000;

	// List of IP addresses for each Server instance.
	private String[] ips = new String[NUM_SERVERS];
	// The Server's current message timestamp.
	private int[] timestamp = {0, 0, 0, 0, 0, 0, 0};
	// The status of each channel to other Servers. The boolean corresponding to the Server's ID is irrelevant.
	private boolean[] closedChannels = new boolean[NUM_SERVERS];
	// Each of the objects stored in the repository.
	private ArrayList<Obj> objects = new ArrayList<Obj>();
	// Buffer for undelivered messages.
	private ArrayList<Message> buffer = new ArrayList<Message>();
	// List of threads connecting to other Servers. The index corresponding to the Server's ID should be null.
	private ServerThread[] threads = new ServerThread[NUM_SERVERS];

	// You can safely ignore these; this is for synchronization purposes.
	int numDelivered = 0;
	int numPrepared = 0;
	int serverID;
	int token = 0;

	public static void main(String[] args) {
		// Terminates if there is an incorrect number of arguments.
		if (args.length != 2) {
			System.out.println("Incorrect number of arguments!");
			System.exit(0);
		}
		new Server(args);
	}

	public Server(String[] args) {
		serverID = Integer.parseInt(args[1]);
		System.out.println("Server " + serverID + " started.");
		int port = COORDINATOR_BASE_PORT + serverID;
		try {
			// Attempts to connect to coordinator
			System.out.println("Attempting to connect to " + args[0] + " on port " + port);
			Socket server = new Socket(InetAddress.getByName(args[0]), port);
			
			System.out.println("Just connected to " + server.getRemoteSocketAddress());
			
			// Create output stream to coordinator and send coordinator the server IP address
			OutputStream outToCoordinator = server.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToCoordinator);
			
			String address = InetAddress.getLocalHost().getHostAddress().trim();
			out.writeUTF(address);
			
			// Create input stream from coordinator and listens until the IP addresses are all obtained
			InputStream inFromCoordinator = server.getInputStream();
			DataInputStream in = new DataInputStream(inFromCoordinator);

			assembleIps(in.readUTF());
			System.out.println("Ready to establish connections!");

			// Create each thread.		
			// The port for each pair will be 7000 + 100 * host + server.

			// Each server will make a host thread for any server with an ID number greater than it.
			// Otherwise, it will make a server thread to connect to all servers with a higher ID.
			for (int i = 0; i < NUM_SERVERS; i++) {
				if (serverID <= i) {
					threads[i] = new ServerThreadHost(i, i, BASE_PORT + 100 * serverID + i, this);
				} else if (serverID > i) {
					threads[i] = new ServerThreadClient(i, i, BASE_PORT + 100 * i + serverID, this, ips[i]);
				}
			}

			System.out.println("Threads created! Waiting to start...");
			System.out.println("Starting threads...");

			// Disconnect from coordinator; it is no longer needed.
			server.close();
			
			// Wait for all threads to connect, then start.
			for(ServerThread thread : threads) {
				if(thread != null)	thread.start();
			}
			
			// Wait for channels between all threads to be created before proceeding.
			while(numPrepared < NUM_SERVERS) {
				System.out.print("");
			}
				
			syncPrint("Ready to send and receive messages.");

			// Create thread to communicate with clients.
			ServerToClientThread scThread = new ServerToClientThread(BASE_PORT + serverID, serverID, this);

			// HOW TO SEND A MESSAGE TO ANOTHER SERVER:
			// 1. Create a message object.
			// 2. Call the send method corresponding to the thread you want to send to.
				// i.e. sendMessage(new Message(timestamp, 0, "Hello, world!"), 1);
			// 3. The other Server will then respond with either "Received" or "Failed"
			//    If "Received" the method will return true. If "Failed" it will return false.

			boolean active = true;
			Scanner kb = new Scanner(System.in);
			while (active) {
				System.out.println("Input the channel which you with to flip (0-6) - input \"exit\" to exit.");
				String nextInput = kb.nextLine();
				if (nextInput.equals("exit")) {
					System.out.println("Exiting!");
					active = false;
				} else {
					int input = Integer.parseInt(nextInput);
					if (input < 0 || input >= NUM_SERVERS) {
						System.out.println("Input out of bounds!");
					} 
					else if(input == serverID) {
						System.out.println("Cannot modify connection with self!");
					}
					else {
						String s = "Channel to Server " + input + " is now ";
						if (closedChannels[input]) {
							s = s + "open!";
						} else {
							s = s + "closed!";
						}
						closedChannels[input] = !closedChannels[input];
					}
				}
			}
			kb.close();

			// TODO: Implement way for user to manually disable channels.
		} catch (UnknownHostException u) {
			System.out.println(u);
			System.exit(1);
		} catch (IOException i) {
			System.out.println(i);
			System.exit(1);
		}

	}
	
	// Sends a message to the chosen recipient, selected by ID. If the recipient is set to the current Server's ID, it returns false.
	// Otherwise, returns the recipient's response after sending the message.
	private boolean sendMessage(Message message, int recipientServerIndex) throws IOException {
		if(recipientServerIndex == serverID) {
			return false;
		}
		return threads[recipientServerIndex].sendMessage(message);
	}

	// Receives the request from a ServerToClientThread and handles it.
	public String getRequest(String request) {
		Scanner req = new Scanner(request);
		int clientID = req.nextInt();
		int requestType = req.nextInt();
		Obj object = new Obj(req.next(), "");
		if(requestType == 1) { // Request type is a read
			req.close();
			Obj pulledObj = readObject(object);
			if (pulledObj.equals(null)) {
				return "Error!";
			} else {
				return pulledObj.toString();
			}
		} else { // Request type is a write
			object.value = req.nextLine(); // set object's value to the requested write value to be written.
			if (writeObject(object, clientID)) {
				req.close();
				return "Successfully wrote!";
			} else {
				req.close();
				return "Write failed!";
			}
		}
	}

	// Returns the object requested by a ServerToClientThread.
	// If the item does not exist yet, returns null.
	public Obj readObject(Obj object) {
		if(objects.contains(object)) {
			return objects.get(objects.indexOf(object));
		}
		else return null;
	}

	// Writes to an object according to the ServerToClientThread which requested it.
	// Takes the source of the request (the ID of the requesting client) for usage in synchronizing when two clients attempt to write at the same time.
	public boolean writeObject(Obj newObject, int source) {
		// TODO: Implement sychronization between replicas upon receiving a new write.
		if(objects.contains(newObject)) {
			objects.get(objects.indexOf(newObject)).value = newObject.value;
			return true;
		} else {
			objects.add(newObject);
			return true;
		}
	}

	public boolean isChannelClosed(int channel) {
		return closedChannels[channel];
	}

	// Synchronized method to print to console. I'm not enirely sure this method is necessary, but I've been using it since Project 1.
	private synchronized void syncPrint(String s) {
		System.out.println(s);
	}

	// Synchronized method to edit or retrieve current timestamp.
	private synchronized int[] accessTimestamp(int index) {
		if(index != -1) {
			timestamp[index]++;
		}
		return timestamp;
	}
	
	// Method for threads to indicate they are ready.
	public synchronized void markReady() {
		numPrepared ++;

		/*
		//if all connections are made
		if(numPrepared == NUM_SERVERS) {
			//send a message to the succeeding server
			int toServer = (serverID + 1) % NUM_SERVERS;
			Message message = new Message(timestamp, 1, "test message "+serverID);

			try {
				sendMessage(message, toServer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		*/
	}

	// Gets a new message from thread and either adds it to the buffer or marks it as delivered.
	public synchronized void receiveMessage(String incoming) { 
		Message newMessage = Message.fromString(incoming);
		attemptDeliver(newMessage, false);
	}
	
	// Delivers a message if the current time is causally greater than the timestamp
	// of the message. Otherwise, adds it to the buffer.
	// message is the message to be delivered; false indicates whether the message was retrived from the buffer.
	// If fromBuffer, attemptDeliver will not call clearBuffer to prevent recursively calling it.
	private synchronized int[] attemptDeliver(Message message, boolean fromBuffer) {
		if (message == null) {
			accessTimestamp(serverID);
			return accessTimestamp(-1);
		}
		int newTimestamp = compareTimestamp(message.timestamp);
		if (newTimestamp != -1) {
			int[] newStamp = accessTimestamp(newTimestamp);
			syncPrint("Message " + message.text + " was delivered. New timestamp is " + Arrays.toString(newStamp));
			numDelivered ++;
			if (!fromBuffer) {
				accessBuffer(message, true);
			}
			return accessTimestamp(-1);
		} else {
			syncPrint("Message \"" + message.text + "\" was placed into the buffer. Current timestamp is " + Arrays.toString(accessTimestamp(-1)));
			accessBuffer(message, false);
		}
		return null;
	}

	// Iterates through the buffer and attempts to deliver each message currently pending delivery. 
	// If clear is false, the program will attempt to clear the buffer. Otherwise, it will add an item to the buffer.
	private synchronized void accessBuffer(Message newMessage, boolean clear) {
		if (!clear) {
			buffer.add(newMessage);
			return;
		}
		//System.out.println("Clearing buffer");
		for (int i = buffer.size() - 1; i >= 0; i--) {
			if(attemptDeliver(buffer.remove(i), true) != null) {
				i = buffer.size() - 1;
			}
		}
		//System.out.println("Done clearning buffer");
	}

	// Compares the timestamp of the given message to the current server's timestamp.
	// Returns true if the client timestamp is causally earlier than the current timestamp.
	// Delivers a timestamp if a single item in the new timestamp 
	// is exactly one greater than the current timestamp.
	private synchronized int compareTimestamp(int[] newMessage) {
		int numLarger = 0;
		int largerIndex = 0;
		int[] currentTimestamp = accessTimestamp(-1);
		for (int i = 0; i < 4; i++) {
			if (newMessage[i] > currentTimestamp[i]) {
				if (newMessage[i] == currentTimestamp[i] + 1) {	
					numLarger ++;
					largerIndex = i;
				} else {
					return -1;
				}
			}
		}
		
		if (numLarger == 1) {
			return largerIndex;
		}
		
		return -1;

	}

	// Populates the ips array using the list of addresses received from the coordinator.
	private void assembleIps(String input) {
		Scanner string = new Scanner(input);
		for(int i = 0; i < NUM_SERVERS; i++) {
			ips[i] = string.next();
		}
		
		System.out.println("IP addresses: ");
		for(int i = 0; i < NUM_SERVERS; i++) {
			System.out.println("Server " + i + ": " + ips[i]);
		}
		string.close();
	}
}