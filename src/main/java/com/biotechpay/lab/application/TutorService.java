package com.biotechpay.lab.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Cezi tutor backend. Talks to OpenRouter on the SERVER side so the API key
 * never reaches the browser. The key is read from the environment via
 * {@code portujava.api-key} (which maps to the OPENROUTER_API_KEY env var) and is
 * never hardcoded or logged.
 */
@Service
public class TutorService {

    private final String apiKey;
    private final String model;
    private final String referer;
    private final RestClient client;

    public TutorService(
            @Value("${portujava.api-key:}") String apiKey,
            @Value("${portujava.model:openai/gpt-4o-mini}") String model,
            @Value("${portujava.base-url:https://openrouter.ai/api/v1}") String baseUrl,
            @Value("${portujava.referer:http://localhost:8080}") String referer) {
        this.apiKey = Objects.requireNonNull(apiKey, "apiKey");
        this.model = Objects.requireNonNull(model, "model");
        this.referer = Objects.requireNonNull(referer, "referer");

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(4000);
        factory.setReadTimeout(15000);
        this.client = RestClient.builder()
                .baseUrl(Objects.requireNonNull(baseUrl, "baseUrl"))
                .requestFactory(factory)
                .build();
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @SuppressWarnings("null")
    public String explain(String concept, String code, String explanation, String event) {
        String system = """
                Você é Cezi, Engenheiro de Software - professor, ensinando Java 17 a iniciantes em português do Brasil.
                Fale em primeira pessoa, como um professor ao lado do aluno, de forma simples e acolhedora.
                Explique em linguagem natural o conceito do passo atual e o porquê dele, no máximo 2 frases curtas.
                Não use emojis. Não escreva blocos de código nem reescreva o código: apenas explique.
                """;
        String userMsg = "Conceito do passo: " + nz(concept) + "\n"
                + "Código da etapa:\n" + nz(code) + "\n"
                + "Nota técnica de apoio: " + nz(explanation) + "\n"
                + "Momento: " + nz(event) + "\n"
                + "Escreva agora a fala curta do Cezi para o aluno.";

        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 180,
                "temperature", 0.5,
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user", "content", userMsg)));

        Map<?, ?> response = client.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", referer)
                .header("X-Title", "Cezi Engenheiro de Software - professor - PortuJava Game")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        return extractContent(response);
    }

    private String extractContent(Map<?, ?> response) {
        if (response == null) {
            return "";
        }
        Object choices = response.get("choices");
        if (choices instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> choice) {
            Object message = choice.get("message");
            if (message instanceof Map<?, ?> msg) {
                Object content = msg.get("content");
                if (content != null) {
                    return content.toString().trim();
                }
            }
        }
        return "";
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}
