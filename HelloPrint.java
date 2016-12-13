public class HelloPrint extends LispSubr {
    public HelloPrint() {
        super("helloprintenlainen", 0, 0);
    }

    public LispObject run(LispObject[] o) {
        System.out.println("hej");
        return null;
    }
}
