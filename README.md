# SelfAssessmentTool
A self assessment tool for COMP112 for Victoria University.

##How to make tasks?
A sample task looks like this:
```java
/**
 * Welcome to our self assessment tool. Fill in the method below with 42 to pass this test.
 */
@Task(name="0. Demo 0")
public abstract class Demo0 {
    @Test
    public void testThings() {
        assertTrue("Should be 42",getAnswer()==42);
    }
    abstract int getAnswer();
}

```
The `@Task` annotation supports two arguments: 

`name` - The name shown in the sidebar and above the code

`showModifiers` - true to show modifiers such as `public`, false to hide.

The `@Task` annotation sits above the class declaration. 
You then need to create an abstract class.

Any method with `@Test` above it will be treated as a JUnit Test, and will show
to the student, and can then at a glance the student can see if they have passed
or failed that task. You can use normal JUnit assertions as the code
is run via JUnit to get a pass or fail.

To get a student to fill in a field, make it `abstract`, and it will automatically
be moved to the section of the website that they can edit.

To get a student to fill in a class, annotate it as `@ClassToComplete`.
For example, 
```java
/**
 * A task based on a swen221 self assessment
 */
@Task(name="2. Template Method 1")
public abstract class TemplateMethod1 {
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
        assertTrue("on data: "+data+" expected: "+expected+" result: "+result, expected.equals(result));
    }
}
```
A note about `@ClassToComplete` is that the class itself does not need to compile correctly, and as a result you can
have a abstract method inside a non abstract class. We opted for this route to keep methods consistent.

##How does it work?
An annotation processor pre-processes the assignment code and generates a task from it, by parsing the elements from the source. 
This task is then cached, and when the user picks a task, it is rendered and the user can enter code. At this point, when they
make a change, their code is sent back and merged with the processed task, and compiled and run, with its output piped back to 
the site for display.