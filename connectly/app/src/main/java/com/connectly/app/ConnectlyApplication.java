package com.connectly.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.connectly")
@EntityScan("com.connectly.infrastructure")
@EnableJpaRepositories("com.connectly.infrastructure")
@EnableScheduling
public class ConnectlyApplication {
    public static void main(String[] args) { SpringApplication.run(ConnectlyApplication.class, args); }
}
