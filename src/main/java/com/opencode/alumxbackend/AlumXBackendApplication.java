package com.opencode.alumxbackend;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.sql.Connection;


@AllArgsConstructor
@SpringBootApplication
public class AlumXBackendApplication implements CommandLineRunner {

    private final DataSource dataSource;

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        System.setProperty("DB_URL", dotenv.get("DB_URL"));
        System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
        System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.out.println("🔍 DB_URL = " + System.getProperty("DB_URL"));
        SpringApplication.run(AlumXBackendApplication.class, args);
    }

    @Override
    public void run(String... args) {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("✅ DATABASE CONNECTED SUCCESSFULLY");
            System.out.println("📌 DB Name: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("📌 DB URL: " + connection.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("❌ DATABASE CONNECTION FAILED");
            e.printStackTrace();
            System.exit(1); // stop app if DB is broken
        }
    }
}
