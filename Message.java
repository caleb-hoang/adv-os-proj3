// Message class. Contains a timestamp represented as an int[] and a message represented as a String.
import java.util.Scanner;

public class Message {

    public static int NUM_SERVERS = 7;
    public int[] timestamp;
    public String text;
    public int sender;

    public Message(int[] timestamp, String message, int serverID) {
        this.timestamp = timestamp;
        this.text = message;
        sender = serverID;
    }

    public String toString() {
        String output = "";
        for(int i = 0; i < timestamp.length; i++) {
			output = output + timestamp[i] + " ";
		}
        output = output + sender + " ";
        return output + text;
    }

    public static Message fromString(String input) {
        //System.out.println("Converting string to message!");
        int[] timestamp = new int[NUM_SERVERS];
        Scanner string = new Scanner(input);

        for(int i = 0; i < NUM_SERVERS; i++) {
            timestamp[i] = string.nextInt();
        }
        int sender = string.nextInt();
        String text = string.nextLine();
        string.close();

        return new Message(timestamp, text, sender);
    }
}
