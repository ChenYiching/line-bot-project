package com.example.bot.spring.covidcases;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


@SpringBootApplication
public class CovidCasesApplication {
    private final Logger log = LoggerFactory.getLogger(CovidCasesApplication.class);
    static Path downloadedContentDir;

    public static void main(String[] args) throws IOException {
        downloadedContentDir = Files.createTempDirectory("line-bot");
        SpringApplication.run(CovidCasesApplication.class, args);
    }


}
