package io.temporal.samples.helloSignal;

import io.temporal.activity.ActivityCancellationType;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.CancellationScope;
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
    CancellationScope cancelScope =
        Workflow.newCancellationScope(
            () -> {
              while (true) {
                if (exit) {
                  return;
                }
                String INIT = "INIT";
                Workflow.await(() -> signals.contains(INIT));
                System.out.println("Process signal: " + INIT);
                greetings.add(greetingActivity.composeGreeting(INIT));
                System.out.println("Processed signals: " + greetings.toString());

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
              }
            });
    try {
      cancelScope.run();
    } catch (Exception e) {
      System.out.println("CancelScope - cancel");
      cancelScope.cancel();
      System.out.println("CancelScope - cancel - done");
    }
  }

  @Override
  public void signalChange(String name) {
    signals.add(name);
  }

  @Override
  public void exit() {
    exit = true;
  }
}
