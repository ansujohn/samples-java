package io.temporal.samples.helloSignal;

import java.util.List;

public class CleanupWorkflowImpl implements CleanupWorkflow {

  @Override
  public void cleanUp(List<String> signals) {
    System.out.println("Do cleanup " + signals);
  }
}
