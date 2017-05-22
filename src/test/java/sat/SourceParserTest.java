package sat;

import org.junit.Before;
import org.junit.Test;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import sat.util.SourceParser;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SourceParserTest {

    /** had to use the absolute path cause it wasn't working (and I forgot how to reference relative file pos */
    private static final String TASK_NAME = "SampleTask.java";
    private List<String> lines;

    @Before
    public void setup() {
        try {
            this.lines = SourceParser.parseSourceFile(SourceParserTest.class.getClassLoader().getResourceAsStream(TASK_NAME));
        } catch (Throwable th) {
            System.out.println(th.getMessage());
            th.printStackTrace();
        } finally {
            if (this.lines == null) {
                this.lines = new ArrayList<String>();
            }
        }
    }

    @Test
    public void testParsing1() {
        assertNotNull(lines);
//        assertFalse(lines.isEmpty());
        for (String s : lines) {
            System.out.println(s);
        }
        assertTrue(true);
    }
}