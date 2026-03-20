package com.example.FakeCommerce.config;

import org.springframework.context.annotation.Configuration;
import io.github.cdimascio.dotenv.Dotenv;


@Configuration
public class EnvConfig {
    
    public EnvConfig(){
        Dotenv dotenv = Dotenv.load();

        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", "DB_PASSWORD");
        System.setProperty("PORT", "PORT");
        System.setProperty("LICENSE_KEY", "LICENSE_KEY");
    }
}
