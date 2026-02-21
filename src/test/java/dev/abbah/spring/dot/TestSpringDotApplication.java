package dev.abbah.spring.dot;

import org.springframework.boot.SpringApplication;

public class TestSpringDotApplication {

  public static void main(String[] args) {
    SpringApplication.from(SpringDotApplication::main)
                     .with(TestcontainersConfiguration.class)
                     .run(args);
  }

}
