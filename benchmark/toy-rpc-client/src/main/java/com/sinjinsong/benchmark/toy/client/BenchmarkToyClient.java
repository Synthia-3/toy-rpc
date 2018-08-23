package com.sinjinsong.benchmark.toy.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author sinjinsong
 * @date 2018/8/23
 */
@SpringBootApplication
@Slf4j
public class BenchmarkToyClient implements CommandLineRunner {
    @Autowired
    private ToyClient client;

    public static void main(String[] args) throws InterruptedException {
        SpringApplication app = new SpringApplication(BenchmarkToyClient.class);
        app.setWebEnvironment(false);
        app.run(args);
    }

    @Override
    public void run(String... strings) throws Exception {
        int threads = Integer.parseInt(strings[0]);
        int requestTotal = Integer.parseInt(strings[1]);
        int measurementIterations = Integer.parseInt(strings[2]);
        client.run(threads, requestTotal,measurementIterations);
//        client.run(1,1,1);
        System.exit(0);
    }
}