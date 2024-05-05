import java.io.*;

// Creates and manages threads to manage the collection of Client ip addresses. 
public class Coordinator {
	public static final int NUM_SERVERS = 2;	// -> 7
	public static final int NUM_CLIENTS = 1;	// -> 5

	private static String[] ips = new String[NUM_SERVERS];
	private static int numWaiting = NUM_SERVERS;
	private static CoordinatorThread[] threads;
	private static Boolean ready = false;
	private static int numReady = 0;

	public static void main(String[] args) {
		try {
			System.out.println("Creating threads");
			threads = new CoordinatorThread[NUM_SERVERS];

			ManagerThread manager = new ManagerThread();
			manager.start();

			for (int i = 0; i < NUM_SERVERS; i++) {
				threads[i] = new CoordinatorThread(8000 + i, i, manager);
			}

			for (int i = 0; i < NUM_SERVERS; i++) {
				threads[i].start();
			}
			// writeIPsToFile();
		} catch (IOException e) {
			System.out.println("IO Exception detected!");
			System.out.println(e);
		}
	}
	
	// Synchronized method used to indicate that a thread is ready to proceed.
	public synchronized static void isReady() {
		numReady ++;
	}
	
	// Unused method which formerly indicated that a thread was ready to proceed.
	public synchronized static void prompt() {
		ready = true;
	}

	// Method used to add a client IP address to the list of IP addresses.
	public synchronized static int logIP(String ip, int index) {
		ips[index] = ip;
		numWaiting --;
		if(numWaiting == 0) writeIPsToFile();
		return numWaiting;
	}
	
	// Getter method for IP address array.
	public synchronized static String[] getIPs() {
		return ips;
	}

	// Writes the IP addresses for each
	public static void writeIPsToFile() {
		try {
			FileWriter addresses = new FileWriter("ips.txt");
			for (String ip : ips) {
				addresses.write(ip + "\n");
			}
			addresses.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
		} catch (IOException i) {
			System.out.println("Write failed.");
		}
	}
}
