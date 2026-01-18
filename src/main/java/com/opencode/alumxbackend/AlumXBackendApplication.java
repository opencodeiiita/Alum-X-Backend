package com.opencode.alumxbackend;

import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.AllArgsConstructor;


@AllArgsConstructor
@SpringBootApplication
@ComponentScan(
    basePackages = "com.opencode.alumxbackend",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.opencode\\.alumxbackend\\.basics\\..*"
    )
)
@EnableTransactionManagement
public class AlumXBackendApplication implements CommandLineRunner {

    private final DataSource dataSource;

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        setIfPresent("DB_URL", dotenv.get("DB_URL"));
        setIfPresent("DB_USERNAME", dotenv.get("DB_USERNAME"));
        setIfPresent("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
        System.out.println("🔍 DB_URL = " + System.getProperty("DB_URL"));

        SpringApplication.run(AlumXBackendApplication.class, args);
    }

    private static void setIfPresent(String key, String value) {
        if (value != null && !value.isBlank()) {
            System.setProperty(key, value);
        }
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
