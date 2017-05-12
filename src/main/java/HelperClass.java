import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HelperClass {
    public void print(Stream<?> stream) {
        System.out.println(stream.map(Object::toString).collect(Collectors.joining(",")));
    }
}
