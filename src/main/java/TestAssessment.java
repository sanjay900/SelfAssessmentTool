import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestAssessment extends SelfAssessment{
    public List<String> data = new ArrayList<>(Arrays.asList("b","c","d","a"));

    @Override
    public String getCodeToDisplay() {
        return "public List<String> data = new ArrayList<>(Arrays.asList(\"b\",\"c\",\"d\",\"a\"))";
    }
}
