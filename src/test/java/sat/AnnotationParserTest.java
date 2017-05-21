package sat;

import org.junit.Test;
import sat.compiler.JavaRunner;

/**
 * Created by sanjay on 21/05/17.
 */
public class AnnotationParserTest {
    @Test
    public void testAssessment() {
        AbstractTask task = JavaRunner.getTask("SampleTask",
                "public void foo() {" +
                "System.out.println(\"Test code, running foo\");" +
                "}",JavaRunner.class.getClassLoader().getResourceAsStream("SampleTask.java"));
        task.run();
        task = JavaRunner.getTask("SampleTask",JavaRunner.class.getClassLoader().getResourceAsStream("SampleTask.java"));
//        task.run();
        System.out.println(task.getCodeToDisplay());
    }
}
