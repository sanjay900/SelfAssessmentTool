package sat.webserver;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by Sanjay on 24/06/2017.
 */
@Data
public class ConsoleUpdateResponse {
    @NonNull
    String text;
    @NonNull
    boolean clear;
    String id = "console";
}
