package sat.webserver;

import lombok.Getter;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sanjay on 22/05/17.
 */
@Getter
public class JUnitRunListener extends RunListener {
    private List<TestResult> results = new ArrayList<>();
    private static final Description FAILED = Description.createTestDescription("failed", "failed");

    @Override
    public void testFailure(Failure failure) throws Exception {
        failure.getDescription().addChild(FAILED);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        results.add(new TestResult(description.getMethodName(),!description.getChildren().contains(FAILED)));
    }
}
