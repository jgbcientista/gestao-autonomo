package br.com.auth.service;

import br.com.auth.dto.PythonPredictionRequest;
import br.com.auth.dto.PythonPredictionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@ConditionalOnProperty(name = "ai.service.enabled", havingValue = "true")
public class PythonAiService {

    private final WebClient webClient;
    private final int timeout;

    public PythonAiService(
            @Value("${ai.service.url}") String aiServiceUrl,
            @Value("${ai.service.timeout:5000}") int timeout) {
        this.webClient = WebClient.builder()
                .baseUrl(aiServiceUrl)
                .build();
        this.timeout = timeout;
        log.info("PythonAiService inicializado com URL: {}", aiServiceUrl);
    }

    public PythonPredictionResponse predict(PythonPredictionRequest request) {
        try {
            return webClient.post()
                    .uri("/predict")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(PythonPredictionResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();
        } catch (Exception e) {
            log.error("Erro ao chamar servico Python AI: {}", e.getMessage());
            return null;
        }
    }

    public Mono<PythonPredictionResponse> predictAsync(PythonPredictionRequest request) {
        return webClient.post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PythonPredictionResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .doOnError(e -> log.error("Erro async ao chamar servico Python AI: {}", e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    public boolean isHealthy() {
        try {
            String status = webClient.get()
                    .uri("/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(3000))
                    .block();
            return status != null && status.contains("UP");
        } catch (Exception e) {
            log.warn("Python AI service indisponivel: {}", e.getMessage());
            return false;
        }
    }

    public PythonPredictionRequest buildRequest(Long userId, String ipAddress, String userAgent,
                                                 int loginAttempts, boolean newDevice, boolean newLocation) {
        LocalDateTime now = LocalDateTime.now();
        return PythonPredictionRequest.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .hourOfDay(now.getHour())
                .dayOfWeek(now.getDayOfWeek().getValue() % 7)
                .loginAttemptsLastHour(loginAttempts)
                .isNewDevice(newDevice)
                .isNewLocation(newLocation)
                .isVpn(false)
                .isTor(false)
                .sessionDurationAvg(300.0)
                .pagesPerSessionAvg(5.0)
                .timeSinceLastLoginHours(24.0)
                .build();
    }
}
