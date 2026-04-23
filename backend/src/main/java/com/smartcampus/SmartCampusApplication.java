package com.smartcampus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Smart Campus Operations Hub — Main Application Entry Point
 * IT3030 PAF Assignment 2026
 */
@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.smartcampus.repository")
public class SmartCampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartCampusApplication.class, args);
    }
}
