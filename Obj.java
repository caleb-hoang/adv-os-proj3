import java.util.Scanner;
public class Obj {
    public String name;
    public String value;
    public int approved = 0;
    public boolean reserved = false;

    public Obj(String objName, String objValue) {
        name = objName;
        value = objValue;
    }

    public synchronized boolean reserve() {
        if(reserved) {
            return false;
        } else {
            reserved = true;
            approved = 0;
            return true;
        }
    }

    public void unreserve() {
        reserved = false;
        approved = 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Obj)) return false;
        Obj object = (Obj) other;
        return name.equals(object.name);
    }

    public String toString() {
        return name + " " + value;
    }

    public static Obj fromString(String input) {
        Scanner object = new Scanner(input);
        Obj newObject = new Obj(object.next(), object.next());
        object.close();
        return newObject;
    }
}
