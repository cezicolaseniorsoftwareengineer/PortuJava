package com.biotechpay.lab.api;

import com.biotechpay.lab.application.TutorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Browser-facing endpoint for the Cezi Engenheiro de Software - professor tutor. The browser posts the
 * current step here; the server calls OpenRouter with the secret key. The key is
 * never exposed to the client. When no key is configured, responds with
 * {@code ok=false} so the front-end falls back to its built-in explanations.
 */
@RestController
@RequestMapping("/api/tutor")
@CrossOrigin(origins = "*")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @PostMapping("/explain")
    public ResponseEntity<Map<String, Object>> explain(@RequestBody(required = false) Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        if (!tutorService.isConfigured()) {
            result.put("ok", false);
            result.put("reason", "no_api_key");
            return ResponseEntity.ok(result);
        }

        Map<String, String> req = request == null ? Map.of() : request;
        try {
            String text = tutorService.explain(
                    req.getOrDefault("concept", ""),
                    req.getOrDefault("code", ""),
                    req.getOrDefault("explanation", ""),
                    req.getOrDefault("event", ""));

            if (text == null || text.isBlank()) {
                result.put("ok", false);
                result.put("reason", "empty_response");
            } else {
                result.put("ok", true);
                result.put("text", text);
            }
        } catch (Exception e) {
            result.put("ok", false);
            result.put("reason", "upstream_error");
        }
        return ResponseEntity.ok(result);
    }
}
