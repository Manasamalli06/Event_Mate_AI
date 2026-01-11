package com.example.eventmateai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;   // ADD THIS

@SpringBootApplication
@EnableScheduling   // ADD THIS
public class EventmateaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventmateaiApplication.class, args);
    }

}
