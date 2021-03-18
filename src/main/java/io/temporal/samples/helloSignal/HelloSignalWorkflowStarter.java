package io.temporal.samples.helloSignal;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;

public class HelloSignalWorkflowStarter {

  public static void main(String[] args) throws Exception {
    // This gRPC stubs wrapper talks to the local docker instance of the Temporal service.
    WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();
    // WorkflowClient can be used to start, signal, query, cancel, and terminate Workflows.
    WorkflowClient client = WorkflowClient.newInstance(service);

    // In a real application use a business ID like customer ID or order ID
    // Generate random int value from min to max
    int max = 60, min = 31;
    int random_int = (int) (Math.random() * (max - min + 1) + min);

    String workflowId = "signal-workflow-" + random_int;
    System.out.println("workflowId = " + workflowId);

    WorkflowOptions options =
        WorkflowOptions.newBuilder()
            .setWorkflowId(workflowId)
            .setTaskQueue(TaskQueues.HELLO_SIGNAL_TASK_QUEUE)
            .build();

    // WorkflowStubs enable calls to methods as if the Workflow object is local, but actually
    // perform an RPC.
    HelloSignalWorkflow workflow = client.newWorkflowStub(HelloSignalWorkflow.class, options);
    // Synchronously execute the Workflow and wait for the response.
    WorkflowClient.start(workflow::start);
    workflow.signalChange("INIT");
    workflow.signalChange("STATE1");
    // workflow.signalChange("STATE2");
    // workflow.exit(); // sends exit signal

    System.exit(0);
  }
}
