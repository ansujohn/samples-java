package io.temporal.samples.helloSignal;

import static io.temporal.workflow.Async.procedure;

import io.temporal.activity.ActivityCancellationType;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.CancellationScope;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class HelloSignalWorkflowImpl implements HelloSignalWorkflow {

  private final List<String> signals = new ArrayList<>();
  private final List<String> greetings = new ArrayList<>();
  boolean exit = false;

  ActivityOptions options =
      ActivityOptions.newBuilder()
          .setScheduleToCloseTimeout(Duration.ofSeconds(180)) // overall wait 3mts
          .setHeartbeatTimeout(Duration.ofSeconds(30))
          .setCancellationType(ActivityCancellationType.WAIT_CANCELLATION_COMPLETED)
          .build();

  private final GreetingActivity greetingActivity =
      Workflow.newActivityStub(GreetingActivity.class, options);

  @Override
  public void start() {
    System.out.println("Starting the workflow: ");
    String INIT = "INIT";
    Workflow.await(() -> signals.contains(INIT));
    System.out.println("Process signal: " + INIT);
    greetings.add(greetingActivity.composeGreeting(INIT));
    System.out.println("Processed signals: " + greetings.toString());

    CancellationScope cancelScope =
        Workflow.newCancellationScope(
            () -> {
              String STATE1 = "STATE1";
              Workflow.await(() -> signals.contains(STATE1));
              System.out.println("Process signal: " + STATE1);
              greetings.add(greetingActivity.composeGreeting(STATE1));
              System.out.println("Processed signals: " + greetings.toString());

              String STATE2 = "STATE2";
              Workflow.await(() -> signals.contains(STATE2));
              System.out.println("Process signal: " + STATE2);
              greetings.add(greetingActivity.composeGreeting(STATE2));
              System.out.println("Processed signals: " + greetings.toString());

              String STATE3 = "STATE3";
              Workflow.await(() -> signals.contains(STATE3));
              System.out.println("Process signal: " + STATE3);
              greetings.add(greetingActivity.composeGreeting(STATE3));
              System.out.println("Processed signals: " + greetings.toString());
            });
    try {
      cancelScope.run();
    } catch (Exception e) {
      System.out.println("CancelScope - cancel");
      cancelScope.cancel();

      System.out.println("Workflow is Cancelled");
      CancellationScope detached =
          Workflow.newDetachedCancellationScope(
              () -> {
                // clean up logic
                ChildWorkflowOptions options =
                    ChildWorkflowOptions.newBuilder()
                        .setTaskQueue(TaskQueues.HELLO_SIGNAL_TASK_QUEUE)
                        .build();
                Promise<Void> promise =
                    procedure(
                        Workflow.newChildWorkflowStub(CleanupWorkflow.class, options)::cleanUp,
                        signals);
                promise.get();
              });
      detached.run();
      System.out.println("CancelScope - cancel - done");
    }

  }

  @Override
  public void signalChange(String name) {
    signals.add(name);
  }

  @Override
  public String querySignals() {
    return signals.toString();
  }

  @Override
  public void exit() {
    exit = true;
  }
}
