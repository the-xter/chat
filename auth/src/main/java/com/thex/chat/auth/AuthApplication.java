package com.thex.chat.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// to remove log message "Using generated security password...":
//@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
