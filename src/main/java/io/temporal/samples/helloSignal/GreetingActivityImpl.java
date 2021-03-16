package io.temporal.samples.helloSignal;

public class GreetingActivityImpl implements GreetingActivity {

  @Override
  public String composeGreeting(String name) {
    return "Hello " + name;
  }
}
