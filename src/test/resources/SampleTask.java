import util.Hidden;

/**
 * Simple task used as a sample for testing.
 * @author Kristian Hansen
 */
public class SampleTask {
    @Hidden(shouldWriteComment=false)
    private int someField = 1;

    public void run() {
        blarg();
        foo();
    }

    @Hidden // random bullshit
    public String blarg() {
        int i = 0;
        while (i < 10) {
            i++;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("hi");
        sb.append("hello there");
        sb.append('b');
        return sb.toString();
    }

    @Hidden(lines="",showFunctionSignature=true,shouldWriteComment=false)
    public void foo() {

    }
    public static void main(String[] args) {
        new SampleTask().run();
    }
}
