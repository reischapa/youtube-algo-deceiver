package com.freezinghipster.youtubealgodeceiver.viewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:app.properties")
public class Main {

    public static void main(String... args) {
        SpringApplication.run(Main.class, args);
    }

}
