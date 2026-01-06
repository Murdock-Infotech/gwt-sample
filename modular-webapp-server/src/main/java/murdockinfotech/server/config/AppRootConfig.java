package murdockinfotech.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.annotation.PropertySource;

/**
 * Root Spring context configuration (non-web beans, shared property loading).
 */
@Configuration
@PropertySource("classpath:application.properties")
public class AppRootConfig {

    /**
     * Enables @Value("${...}") placeholders.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}




