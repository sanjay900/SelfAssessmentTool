import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import sat.util.*;
import sat.*;
import static org.junit.Assert.assertTrue;

/**
 * Created by sanjay on 22/05/17.
 */
@Task(name="TemplateMethod1")
public abstract class TemplateMethod1 extends AbstractTask {
    abstract class A<T>{
        T findMax(List<T> that){
            if(that.isEmpty()){throw new java.util.NoSuchElementException();}
            T candidate=that.get(0);
            for(T s:that){
                candidate=better(candidate,s);
            }
            return candidate;
        }
        abstract T better(T e1, T e2);
    }
    @ClassToComplete
    class B extends A<Integer>{
        abstract Integer better(Integer e1, Integer e2);
    }

    @Test
    public void testCheck(){
        check(3,Arrays.asList(22,12,13,14,55,102,13,3));
        check(-12,Arrays.asList(22,-12,13,14,55,102,13,3));
        check(1, Arrays.asList(1));
    }
    void check(Integer expected,List<Integer> data){
        Integer result=new B().findMax(data);
        assertTrue("on data: "+data+"expected: "+expected+" result: "+result, expected.equals(result));
    }
}
