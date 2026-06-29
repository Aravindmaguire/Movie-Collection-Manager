package com.moviemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This is the ENTRY POINT of our entire application.
 * When you run this class, Spring Boot starts up and does the following:
 * 
 * 1. Starts an embedded Tomcat web server (default port: 8080)
 * 2. Scans all packages under "com.moviemanager" for Spring components
 * 3. Auto-configures everything based on the dependencies in pom.xml
 * 4. Connects to the MySQL database
 * 5. Creates/updates database tables based on our Entity classes
 * 
 * @SpringBootApplication is a powerful annotation that combines THREE annotations:
 * 
 *   @Configuration     - Marks this class as a source of bean definitions
 *   @EnableAutoConfiguration - Tells Spring Boot to auto-configure based on dependencies
 *   @ComponentScan     - Tells Spring to scan this package and sub-packages
 *                        for classes annotated with @Controller, @Service, @Repository, etc.
 * 
 * Think of it as saying: "Hey Spring, this is my main class. 
 * Please set everything up automatically and find all my components."
 */
@SpringBootApplication
public class MovieCollectionManagerApplication {

    /**
     * The main method - where Java always starts execution.
     * SpringApplication.run() does all the heavy lifting:
     * - Creates the Spring application context (the container that manages all objects)
     * - Starts the embedded web server
     * - Makes our REST APIs available at http://localhost:8080
     */
    public static void main(String[] args) {
        SpringApplication.run(MovieCollectionManagerApplication.class, args);
    }
}
