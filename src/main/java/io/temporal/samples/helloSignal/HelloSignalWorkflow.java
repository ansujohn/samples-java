package io.temporal.samples.helloSignal;

import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface HelloSignalWorkflow {
  /**
   * list of stage name strings that were received through the waitForStageChange. This method will
   * block until the number of stages specified are received.
   */
  @WorkflowMethod
  void start();

  /** Receives name through an external signal. */
  @SignalMethod
  void signalChange(String stage);

  @SignalMethod
  void exit();

  @QueryMethod
  String querySignals();
}
