package io.temporal.samples.helloSignal;

import static java.util.Optional.empty;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
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
    workflow.signalChange("STATE2");
    workflow.signalChange("STATE3");
    testEnv.sleep(Duration.ofSeconds(2));

    verify(greetingActivity, times(4)).composeGreeting(any());
  }

  @Test
  public void testCancellation() {

    GreetingActivity greetingActivity = mock(GreetingActivity.class);
    when(greetingActivity.composeGreeting(anyString())).thenReturn("Hello Test");
    worker.registerActivitiesImplementations(greetingActivity);

    // As new mock is created on each workflow task the only last one is useful to verify calls.
    AtomicReference<CleanupWorkflow> lastChildMock = new AtomicReference<>();
    // Factory is called to create a new workflow object on each workflow task.
    worker.addWorkflowImplementationFactory(
        CleanupWorkflow.class,
        () -> {
          CleanupWorkflow child = mock(CleanupWorkflow.class);
          lastChildMock.set(child);
          return child;
        });

    testEnv.start();

    UUID wfId = UUID.randomUUID();
    // Get a workflow stub using the same task queue the worker uses.
    WorkflowOptions workflowOptions =
        WorkflowOptions.newBuilder()
            .setWorkflowId(wfId.toString())
            .setTaskQueue(TEST_QUEUE)
            .setWorkflowTaskTimeout(Duration.ofSeconds(20))
            .build();
    HelloSignalWorkflow workflow =
        client.newWorkflowStub(HelloSignalWorkflow.class, workflowOptions);

    // Start workflow
    WorkflowClient.start(workflow::start);

    workflow.signalChange("INIT");
    workflow.signalChange("STATE1");
    testEnv.sleep(Duration.ofSeconds(5));
    client.newUntypedWorkflowStub(wfId.toString(), empty(), empty()).cancel();
    verify(greetingActivity, times(4)).composeGreeting(any());
  }
}
