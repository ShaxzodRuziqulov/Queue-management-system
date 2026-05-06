package com.example.queuemanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QueueManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueueManagementSystemApplication.class, args);
    }

}
