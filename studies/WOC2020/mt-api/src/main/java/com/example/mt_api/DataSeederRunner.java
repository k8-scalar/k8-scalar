package com.example.mt_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.mt_api.service.SeedService;

@Component
public class DataSeederRunner implements CommandLineRunner {
    @Autowired
    private SeedService seedService;

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0) {
            switch (args[0]) {
                case "seed":
                    seedService.seedData();
                    break;
                case "clear":
                    seedService.clearData();
                    break;
                default:
                    System.out.println("Unknown command: " + args[0]);
            }
        }
    }
}
