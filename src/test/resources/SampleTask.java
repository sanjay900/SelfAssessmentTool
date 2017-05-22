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

    @Hidden(lines="1-4,7-8,10,12",showFunctionSignature=true,shouldWriteComment=false)
    public void foo() {
		final byte b = 9;
		if (b == 9) {
			b = 10;
		}
		String s = blarg();
		something();
		something();
		// sample code just to test line hiding
		do {
			b++;
		} while (b <= 15);
		return;
    }
	private void something() {
		
	}
    public static void main(String[] args) {
        new SampleTask().run();
    }
}
