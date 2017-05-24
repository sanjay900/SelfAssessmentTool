package sat.util;

import lombok.Data;

/**
 * Sent to the web
 */
@Data
public class FileUpdateEvent {
    public boolean updated = true;
}
