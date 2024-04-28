import java.io.*;


// Creates and manages threads to manage the collection of Client ip addresses. 
public class Coordinator {
	private static String[] ips = new String[4];
	private static int numWaiting = 4;
	private static CoordinatorThread[] threads;
	private static Boolean ready = false;
	private static int numReady = 0;

	public static void main(String[] args) {
		try {
			System.out.println("Creating threads");
			threads = new CoordinatorThread[4];
			ManagerThread manager = new ManagerThread();
			manager.start();
			for (int i = 0; i < 4; i++) {
				threads[i] = new CoordinatorThread(8000 + i, i, manager);
			}
	
			for (int i = 0; i < 4; i++) {
				threads[i].start();
			}

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
		return numWaiting;
	}
	
	// Getter method for IP address array.
	public synchronized static String[] getIPs() {
		return ips;
	}
}
