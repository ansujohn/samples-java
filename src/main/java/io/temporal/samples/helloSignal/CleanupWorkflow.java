package io.temporal.samples.helloSignal;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.util.List;

@WorkflowInterface
public interface CleanupWorkflow {
  @WorkflowMethod
  void cleanUp(List<String> signals);
}
