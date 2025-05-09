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
	// The status of each channel to other Servers. The boolean corresponding to the Server's ID is irrelevant.
	private boolean[] closedChannels = new boolean[NUM_SERVERS];
	// Each of the objects stored in the repository.
	private ArrayList<Obj> objects = new ArrayList<Obj>();
	private int[] timestamp = new int[NUM_SERVERS];
	// Buffer for undelivered messages.
	private ArrayList<Message> buffer = new ArrayList<Message>();
	private ArrayList<String> failedWriteRequests = new ArrayList<String>();
	// List of threads connecting to other Servers. The index corresponding to the Server's ID should be null.
	private ServerThread[] threads = new ServerThread[NUM_SERVERS];
	// Indicates whether a Server can be accessed via Client.
	private boolean isOpen = true;
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
				if (serverID < i) {
					threads[i] = new ServerThreadHost(serverID, i, BASE_PORT + 100 * serverID + i, this);
				} else if (serverID > i) {
					threads[i] = new ServerThreadClient(serverID, i, BASE_PORT + 100 * i + serverID, this, ips[i]);
				}
			}

			System.out.println("Threads created! Waiting to start...");
			System.out.println("Starting threads...");

			// Disconnect from coordinator; it is no longer needed.
			server.close();
			
			// Wait for all threads to connect, then start.
			for(ServerThread thread : threads) {
				if(thread != null)	{
					thread.start();
				}
			}
			
			// Wait for channels between all threads to be created before proceeding.
			while(numPrepared < NUM_SERVERS - 1) {
				System.out.print("");
			}

			syncPrint("Ready to send and receive messages.");

			// Create thread to communicate with clients.
			ServerToClientThread scThread = new ServerToClientThread(BASE_PORT + serverID * 100 + serverID, serverID, this);
			scThread.start();
			// HOW TO SEND A MESSAGE TO ANOTHER SERVER:
			// 1. Create a message object.
			// 2. Call the send method corresponding to the thread you want to send to.
			// 3. The other Server will then respond with either "Received" or "Failed"
			//    If "Received" the method will return true. If "Failed" it will return false.

			boolean active = true;
			Scanner kb = new Scanner(System.in);
			while (active) {
				System.out.println("Input the channel which you with to flip (0-6) - input \"exit\" to exit. Input \"client\" to toggle openness to Clients.");
				String nextInput = kb.nextLine();
				if (nextInput.equals("exit")) {
					System.out.println("Exiting!");
					active = false;
				} else if (nextInput.equals("client")) {
					if (isOpen) { // toggles client communication OFF.
						System.out.println("Turning off client input!");
					} else {
						System.out.println("Turning on client input!");
					}
					isOpen = !isOpen;
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
						System.out.println(s);
						closedChannels[input] = !closedChannels[input];
					}
				}
			}
			kb.close();

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
	private boolean sendMessage(Message message, int recipientServerIndex) {
		try {
			if(recipientServerIndex == serverID || isChannelClosed(recipientServerIndex)) {
				System.out.println("Sending to " + recipientServerIndex + " not allowed!");
				return false;
			}
			return threads[recipientServerIndex].sendMessage(message);
		} catch (IOException e) {
			System.out.println("IO Error when sending message!");
			return false;
		}
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
			if (pulledObj == null || pulledObj.value.equals("") || pulledObj.reserved) {
				return "Error!";
			} else {
				return pulledObj.toString();
			}
		} else { // Request type is a write
			object.value = req.nextLine(); // set object's value to the requested write value to be written.
			if (writeObject(object, clientID)) {
				req.close();
				System.out.println("Object written: " + object);
				return "Successfully wrote!";
			} else {
				failedWriteRequests.add(request);
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
	public synchronized boolean writeObject(Obj newObject, int source) {
		if(objects.contains(newObject)) {
			if (broadcastWrite(newObject)) {
				objects.get(objects.indexOf(newObject)).value = newObject.value;
				return true;
			} else {
				return false;
			}
		} else {
			objects.add(newObject);
			newObject.reserved = true;
			if (broadcastWrite(newObject)) {
				newObject.reserved = false;
				return true;
			} else {
				objects.remove(newObject);
				return false;
			}
		}
	}

	public synchronized boolean broadcastWrite(Obj object) {
		System.out.println("attempting to broadcast write!");
		int serverOne = (serverID + 2) % NUM_SERVERS;
		int serverTwo = (serverID + 4) % NUM_SERVERS; 
		System.out.println(objects.contains(object));
		String messageString = "writeRequest " + object.toString();
		Message newMessage = new Message(timestamp, messageString, serverID);
		boolean first = sendMessage(newMessage, serverOne);
		System.out.println("Sent request to Server "+ serverOne);
		boolean second = sendMessage(newMessage, serverTwo);
		System.out.println("Sent request to Server "+ serverTwo);
		if(!first && !second) {
			// write fails automatically
			System.out.println("Write failed!");
			return false;
		}
		int numNeeded = 0;
		if (first && second) {
			numNeeded = 2;
		} else {
			numNeeded = 1;
		}
		System.out.println("num needed: " + numNeeded);
		while (objects.get(objects.indexOf(object)).approved < numNeeded) {
		}
		object.approved = 0;
		System.out.println("All approved!");
		String approvedMessageString = "writeFinalize " + object.toString();
		Message approvedNewMessage = new Message(timestamp, approvedMessageString, serverID);
		if(first) sendMessage(approvedNewMessage, serverOne);
		if(second) sendMessage(approvedNewMessage, serverTwo);
		return true;
	}

	public void processWriteRequest(String messageContent, int sender) {
		Obj object = Obj.fromString(messageContent);
		System.out.println(messageContent);
		String messageString = "writeApprove " + object;
		Message newMessage = new Message(timestamp, messageString, serverID);
		sendMessage(newMessage, sender);
	}

	public void processWriteApprove(String messageContent, int sender) {
		Obj object = Obj.fromString(messageContent);
		objects.get(objects.indexOf(object)).approved ++;
		System.out.println(objects.get(objects.indexOf(object)).approved);
	}

	public void processWriteFinalize(String messageContent) {
		Obj object = Obj.fromString(messageContent);
		if (objects.contains(object)) {
			objects.remove(object);	
		}
		objects.add(object);
		System.out.println("Object written from other Server: " + object);
	}

	public boolean isChannelClosed(int channel) {
		return closedChannels[channel];
	}

	// Synchronized method to print to console. I'm not entirely sure this method is necessary, but I've been using it since Project 1.
	private synchronized void syncPrint(String s) {
		System.out.println(s);
	}
	
	// Method for threads to indicate they are ready.
	public synchronized void markReady() {
		numPrepared ++;
	}

	// Gets a new message from thread and either adds it to the buffer or marks it as delivered.
	public void receiveMessage(Message message) { 
		System.out.println("New message: " + message.toString());
		String text = message.text;
		Scanner messageText = new Scanner(text);
		String messageType = messageText.next();
		String messageContent = messageText.nextLine();
		messageText.close();
		if (messageType.equals("writeRequest")) {
			processWriteRequest(messageContent, message.sender);
		} else if (messageType.equals("writeApprove")) {
			processWriteApprove(messageContent, message.sender);
		} else if (messageType.equals("writeDeny")) {
			// processWriteDeny(messageContent);
		} else if (messageType.equals("writeFinalize")) {
			processWriteFinalize(messageContent);
		}
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
