package sat.autocompletion;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by sanjay on 24/05/17.
 */
@Data
@AllArgsConstructor
public class AutoCompletion {
    String name;
    String value;
    String meta;
    String caption;
    public AutoCompletion(String name, String value, String meta) {
        this(name,value,meta,value);
    }
}
