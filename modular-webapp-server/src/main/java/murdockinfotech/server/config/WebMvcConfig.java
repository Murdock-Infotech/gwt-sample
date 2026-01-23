package murdockinfotech.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Spring MVC configuration for the DispatcherServlet (non-Boot).
 */
@Configuration
@EnableWebMvc
@Import(AppRootConfig.class)
@ComponentScan(basePackages = "murdockinfotech.server.controller")
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    /**
     * Let the servlet container's default servlet serve static resources from src/main/webapp.
     */
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }
}


