package sat.compiler.java;

import lombok.Getter;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import sat.compiler.task.TestResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A class that collects all the information about a junit test and packages it as a list of TestResults.
 */
@Getter
public class JUnitTestCollector extends RunListener {
    private static final Description FAILED = Description.createTestDescription("failed", "failed");
    Consumer<Object> messageQueue;
    public JUnitTestCollector(Consumer<Object> messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        if (failure.getException() instanceof InterruptedException) return;
        System.out.println("Test Failed: "+failure.getMessage());
        failure.getDescription().addChild(FAILED);
        messageQueue.accept(new TestResult(failure.getDescription().getMethodName(),false,failure.getMessage()));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        if (description.getChildren().contains(FAILED)) return;
        System.out.println("Test Passed: "+description.getMethodName());
        messageQueue.accept(new TestResult(description.getMethodName(),true,""));
    }
}
