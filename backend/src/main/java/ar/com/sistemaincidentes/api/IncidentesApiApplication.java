package ar.com.sistemaincidentes.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class IncidentesApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentesApiApplication.class, args);
    }
}
