package Reduce;

import org.junit.Test;

import java.util.stream.*;

import static org.junit.Assert.*;

import javafx.util.Pair;
import sat.compiler.java.annotations.Hidden;
import sat.compiler.java.annotations.Task;

/**
 * return the average value in the stream.
 * Note Lambdas have to be pure
 *      methods do not
 *      Stream try to be pure - so watch for code being pruned!
 * To construct a Pure solution I converted the stream if Integers
 * into a stream of Pair<Integer,Integer> and computed (count,sum)
 * You can map method  int sidemethod(int i)  by using  map(this::sidemethod)
 */
@Task(name="X1. Stream Reduce Pure average ")
public abstract class StreamsReduce3 {
    @Test
    public void testStream1() {
        assertEquals("Should be 6",2.0 ,average(Stream.of(1,2,3)) , 0.01);
    }
    @Test
    public void testStream2() {
        assertEquals("Should be 4",4.0,average(Stream.of(5,5,5,3,2)), 0.01);
    }
    abstract double average(Stream<Integer> stream);

    private double cnt = 0;
    private double sofar =0;
    @Hidden
    public int sidestep(Integer i) {
        sofar+=i;
        cnt+=1;
        return 0;
    }
    @Hidden
    public double sidetest(Stream<Integer> stream) {
        stream.map(this::sidestep).reduce(0,(x,y)->x+y);
        return  sofar/cnt;
    }
    @Hidden
    public double puretest(Stream<Integer> stream) {
        Pair<Integer,Integer> p  = new Pair(0,0);
        System.out.println("Test Start");
        int l = 0;
        Pair<Integer, Integer> g =
                stream.peek(x -> System.out.println("x = " + x.toString())).
                        map(x -> new Pair<Integer, Integer>(0, x)).
                        peek(x -> System.out.println("pair is " + x.toString())).
                        reduce(new Pair<Integer,Integer>(0, 0),
                                (Pair<Integer,Integer> k ,Pair<Integer,Integer> v) ->
                                        ( new Pair<Integer,Integer>(k.getKey()+1 ,k.getValue()+v.getValue()) ));
        double d = (double) g.getValue()/g.getKey();
        System.out.printf("average = %.2f" , d);

        System.out.println("test End " + g);
        return d;
    }

}
