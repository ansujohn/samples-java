package io.temporal.samples.helloSignal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import java.time.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

public class HelloSignalWorkflowTest {
  private TestWorkflowEnvironment testEnv;
  private Worker worker;
  private WorkflowClient client;
  private static final String TEST_QUEUE = "TEST_QUEUE";

  /** Prints a history of the workflow under test in case of a test failure. */
  @Rule
  public TestWatcher watchman =
      new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
          if (testEnv != null) {
            System.err.println(testEnv.getDiagnostics());
            testEnv.close();
          }
        }
      };

  @Before
  public void setUp() {
    testEnv = TestWorkflowEnvironment.newInstance();
    worker = testEnv.newWorker(TEST_QUEUE);
    worker.addWorkflowImplementationFactory(
        HelloSignalWorkflow.class, () -> new HelloSignalWorkflowImpl());
    client = testEnv.getWorkflowClient();
  }

  @After
  public void tearDown() {
    testEnv.close();
  }

  @Test
  public void testSignal() {
    GreetingActivity greetingActivity = mock(GreetingActivity.class);
    when(greetingActivity.composeGreeting(anyString())).thenReturn("Hello Test");

    worker.registerActivitiesImplementations(greetingActivity);
    testEnv.start();

    // Get a workflow stub using the same task queue the worker uses.
    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder()
            .setTaskQueue(TEST_QUEUE)
            .setWorkflowTaskTimeout(Duration.ofSeconds(20))
            .build();
    HelloSignalWorkflow workflow =
        client.newWorkflowStub(HelloSignalWorkflow.class, workflowOptions);

    // Start workflow
    WorkflowClient.start(workflow::start);

    workflow.signalChange("INIT");
    workflow.signalChange("STATE1");
    testEnv.sleep(Duration.ofSeconds(20));

    verify(greetingActivity, times(2)).composeGreeting(any());
  }
}
