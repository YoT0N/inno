package chnu.edu.inno.config;

import org.javers.spring.auditable.AuthorProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import java.util.Optional;


@Configuration
@EnableMongoAuditing
public class AuditConfiguration {

    private static final String CURRENT_USER = "ADMIN";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> Optional.of(CURRENT_USER);
    }

    @Bean
    public AuthorProvider javersAuthorProvider() {
        return () -> CURRENT_USER;
    }
}