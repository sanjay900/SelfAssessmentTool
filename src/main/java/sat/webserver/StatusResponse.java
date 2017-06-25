package sat.webserver;

import lombok.Data;
import lombok.NonNull;

/**
 * Created by Sanjay on 25/06/2017.
 */
@Data
public class StatusResponse {
    String id = "status";
    @NonNull
    boolean running;
}
