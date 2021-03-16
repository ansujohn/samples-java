package io.temporal.samples.helloSignal;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface GreetingActivity {

  String composeGreeting(String name);
}
