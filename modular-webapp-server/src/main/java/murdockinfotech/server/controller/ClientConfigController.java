package murdockinfotech.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes runtime configuration to the browser (loaded before the GWT bootstrap).
 *
 * This exists so the GWT client can target the Spring Boot server origin even when the
 * GWT code is loaded from a different origin (e.g. Super Dev Mode code server :9876).
 */
@RestController
public class ClientConfigController {

    /**
     * Base origin / "context root" used by the browser client to build RPC URLs.
     * Example: http://localhost:8080
     */
    @Value("${modularwebapp.contextRoot:http://localhost:8080}")
    private String contextRoot;

    @GetMapping(value = "/client-config.js", produces = "application/javascript; charset=UTF-8")
    public ResponseEntity<String> clientConfigJs() {
        String normalized = normalizeContextRoot(contextRoot);

        // Emit a tiny JS file that sets a global for the GWT client to read.
        // Escaping: keep it simple by replacing backslashes and single quotes.
        String safe = normalized.replace("\\", "\\\\").replace("'", "\\'");
        String body = ""
                + "(function(){\n"
                + "  window.__MODULAR_WEBAPP_CONTEXT_ROOT__ = '" + safe + "';\n"
                + "})();\n";

        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf("application/javascript; charset=UTF-8"))
                .body(body);
    }

    private static String normalizeContextRoot(String value) {
        if (value == null) {
            return "http://localhost:8080";
        }
        String v = value.trim();
        while (v.endsWith("/")) {
            v = v.substring(0, v.length() - 1);
        }
        return v.isEmpty() ? "http://localhost:8080" : v;
    }
}


