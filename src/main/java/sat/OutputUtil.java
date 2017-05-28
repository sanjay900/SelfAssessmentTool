package sat;

import java.io.*;
import java.util.HashMap;

/**
 * Created by sanjay on 28/05/17.
 */
public class OutputUtil {
    private static HashMap<Thread,StringWriter> map = new HashMap<>();
    static  {
//        PrintStream normal = System.out;
//        System.setOut(new PrintStream(new OutputStream() {
//            @Override
//            public void write(int b) throws IOException {
//                System.err.println("WRITE:"+Thread.currentThread().getName());
//                map.putIfAbsent(Thread.currentThread(),new StringWriter());
//                map.get(Thread.currentThread()).write(b);
//                normal.write(b);
//            }
//        }));
    }
    public static void clearOutput() {
        map.remove(Thread.currentThread());
    }
    public static String getOutput() {
        System.err.println("getOutput:"+Thread.currentThread().getName());
        return map.getOrDefault(Thread.currentThread(),new StringWriter()).toString();
    }
}
