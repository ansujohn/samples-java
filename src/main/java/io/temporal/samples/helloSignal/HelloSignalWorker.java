package io.temporal.samples.helloSignal;

import io.temporal.client.WorkflowClient;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

public class HelloSignalWorker {

  public static void main(String[] args) {

    // WorkflowServiceStubs is a gRPC stubs wrapper that talks to the local Docker instance of the
    // Temporal server.
    WorkflowServiceStubs service = WorkflowServiceStubs.newInstance();

    // WorkflowClient can be used to start, signal, query, cancel, and terminate Workflows.
    WorkflowClient client = WorkflowClient.newInstance(service);

    // Create a Worker factory that can be used to create Workers that poll specific Task Queues.
    WorkerFactory factory = WorkerFactory.newInstance(client);

    // This Worker hosts both Workflow and Activity implementations.
    Worker worker = factory.newWorker(TaskQueues.HELLO_SIGNAL_TASK_QUEUE);
    // Workflows are stateful. So you need a type to create instances.
    worker.registerWorkflowImplementationTypes(HelloSignalWorkflowImpl.class);
    // Activities are stateless and thread safe. So a shared instance is used.
    worker.registerActivitiesImplementations(new GreetingActivityImpl());
    // Start polling the Task Queue.
    factory.start();

    System.out.println("Worker polling on task queue: " + TaskQueues.HELLO_SIGNAL_TASK_QUEUE);
  }
}
