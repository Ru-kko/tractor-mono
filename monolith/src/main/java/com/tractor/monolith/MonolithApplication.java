package com.tractor.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.tractor")
public class MonolithApplication {

  public static void main(String[] args) {
    SpringApplication.run(MonolithApplication.class, args);
  }

}
