import java.util.Scanner;
public class Obj {
    public String name;
    public String value;

    public Obj(String objName, String objValue) {
        name = objName;
        value = objValue;
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
        return new Obj(object.next(), object.next());
    }
}
