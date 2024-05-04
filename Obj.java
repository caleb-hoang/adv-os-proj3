public class Obj {
    public String name;
    public String value;

    public Obj(String objName, String objValue) {
        name = objName;
        value = objValue;
    }

    public boolean equals(Obj object) {
        return name.equals(object.name);
    }

    public String toString() {
        return name + " " + value;
    }
}
