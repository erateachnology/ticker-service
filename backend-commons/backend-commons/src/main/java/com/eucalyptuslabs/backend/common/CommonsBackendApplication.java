package com.eucalyptuslabs.backend.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Base application class enabling useful Spring's features. Create a class inheriting from this one
 * and annotate it with {@link SpringBootApplication} to create a bootable Spring application.
 */
@EnableScheduling
@EnableCaching
@ComponentScan(basePackages = {"com.eucalyptuslabs.backend"})
public class CommonsBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(CommonsBackendApplication.class, args);
  }
}
