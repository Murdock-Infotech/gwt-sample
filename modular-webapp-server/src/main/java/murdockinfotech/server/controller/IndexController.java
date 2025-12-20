package murdockinfotech.server.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Controller to serve the GWT application index page
 */
@RestController
public class IndexController {

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> index() throws IOException {
        Resource resource = new ClassPathResource("static/index.html");
        if (!resource.exists()) {
            // Fallback to webapp directory
            resource = new ClassPathResource("webapp/index.html");
        }
        if (resource.exists()) {
            String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            return ResponseEntity.ok().body(content);
        }
        // Return a simple HTML page if file not found
        return ResponseEntity.ok().body("<!DOCTYPE html><html><head><title>Modular Web Application</title></head><body><h1>GWT Application</h1><p>Index.html not found. Please ensure GWT compilation has completed.</p></body></html>");
    }
}

