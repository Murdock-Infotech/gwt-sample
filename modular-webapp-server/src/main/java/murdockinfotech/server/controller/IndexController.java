package murdockinfotech.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirect "/" to the static index page served from the WAR (src/main/webapp/index.html).
 */
@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }
}

