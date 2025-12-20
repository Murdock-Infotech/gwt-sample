package murdockinfotech.server.config;

import murdockinfotech.server.UserServiceImpl;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring configuration for registering GWT servlets and static resources
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ServletRegistrationBean userServiceServlet() {
        UserServiceImpl servlet = new UserServiceImpl();
        ServletRegistrationBean registration = new ServletRegistrationBean(servlet, "/modularwebapp/userService");
        registration.setName("userService");
        return registration;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static resources from webapp directory (where GWT compiles to)
        // Also serve from static directory for index.html
        registry.addResourceHandler("/**")
                .addResourceLocations(
                    "classpath:/static/",
                    "classpath:/webapp/",
                    "file:src/main/webapp/",
                    "file:target/classes/webapp/"
                );
    }
}

