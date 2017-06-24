package sat.webserver;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class AutocompleteRequest implements Serializable {
    String code;
    String file;
    int line;
    int col;
    List<CompileRequest> files;
}
