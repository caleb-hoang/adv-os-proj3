// Message class. Contains a timestamp represented as an int[] and a message represented as a String.
import java.util.Scanner;

public class Message {

    public static int NUM_SERVERS = 5; // ->7
    public int[] timestamp;
    public String text;
    public int object;

    public Message(int[] timestamp, int object, String message) {
        this.timestamp = timestamp;
        this.text = message;
        this.object = object;
    }

    public String toString() {
        String output = "";
        for(int i = 0; i < timestamp.length; i++) {
			output = output + timestamp[i] + " ";
		}
        return output + text;
    }

    public static Message fromString(String input) {
        int[] timestamp = new int[NUM_SERVERS];
        Scanner string = new Scanner(input);

        for(int i = 0; i < NUM_SERVERS; i++) {
            timestamp[i] = string.nextInt();
        }

        int object = string.nextInt();
        String text = string.nextLine();
        string.close();

        return new Message(timestamp, object, text);
    }
}
