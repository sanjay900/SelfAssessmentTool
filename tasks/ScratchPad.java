import org.junit.Test;
import sat.util.*;
import sat.compiler.annotations.*;

import static org.junit.Assert.*;


/**
 * Welcome to the scratch pad. Use this file for testing methods, as no actuall task is set here.
 */
@Task(name="99. Scratchpad",showModifiers=false)
public abstract class ScratchPad {
    @Test
    public void test() {
        run();
    }
    abstract void run();
}
