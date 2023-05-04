package com.backend.ticker;


import com.eucalyptuslabs.backend.common.CommonsBackendApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TickerBackendApplication extends CommonsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TickerBackendApplication.class, args);
    }

}
