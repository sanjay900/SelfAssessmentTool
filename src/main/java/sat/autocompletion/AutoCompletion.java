package sat.autocompletion;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by sanjay on 24/05/17.
 */
@Data
@AllArgsConstructor
public class AutoCompletion implements Comparable<AutoCompletion> {
    String name;
    String value;
    String meta;
    String caption;
    public AutoCompletion(String name, String value, String meta) {
        this(name,value,meta,value);
    }

    @Override
    public int compareTo(AutoCompletion o) {
        if (meta.equals("variable") && !o.meta.equals("variable")) {
            return -1;
        }
        if (!meta.equals("variable") && o.meta.equals("variable")) {
            return 1;
        }
        return caption.compareTo(o.caption);
    }
}
