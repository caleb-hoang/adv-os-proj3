public class ManagerThread extends Thread {
	public void run() {
		synchronized(this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("All threads have been released!");
		}
	}
}