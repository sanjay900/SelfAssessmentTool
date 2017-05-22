package sat;

import org.junit.Test;
import sat.compiler.JavaRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by sanjay on 21/05/17.
 */
public class AnnotationParserTest {
    @Test
    public void testAssessment() {
        try {
            AbstractTask task = JavaRunner.getTask("SampleTask",
                    "public void foo() {" +
                            "System.out.println(\"Test code, running foo\");" +
                            "}",new FileInputStream("SampleTask.java"));
            task = JavaRunner.getTask("SampleTask",new FileInputStream("SampleTask.java"));
//        task.run();
            System.out.println(task.getCodeToDisplay());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
