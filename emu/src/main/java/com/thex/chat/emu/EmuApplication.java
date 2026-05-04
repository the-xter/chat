package com.thex.chat.emu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EmuApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmuApplication.class, args);
    }
}
