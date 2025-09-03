// src/main/java/com/goormi/routine/kakao/KakaoTalkClient.java
package com.goormi.routine.kakao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoTalkClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * 카카오 "나에게 보내기" (default template)
     * https://kapi.kakao.com/v2/api/talk/memo/default/send
     */
    public void sendToMe(String accessToken, String text) {
        try {
            String templateJson = """
                    {
                      "object_type":"text",
                      "text": %s,
                      "link":{"web_url":"https://goormi.com","mobile_web_url":"https://goormi.com"},
                      "button_title":"지금 열기"
                    }
                    """.formatted(escapeJson(text));

            webClientBuilder.build()
                    .post()
                    .uri("https://kapi.kakao.com/v2/api/talk/memo/default/send")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(BodyInserters
                            .fromFormData("template_object", templateJson))
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnNext(body -> log.info("[KAKAO] sendToMe OK: {}", body))
                    .doOnError(err -> log.error("[KAKAO] sendToMe ERROR: {}", err.getMessage(), err))
                    .block(); // 간단히 동기 호출(스케줄러에서 사용)
        } catch (Exception e) {
            log.error("[KAKAO] sendToMe exception: {}", e.getMessage(), e);
        }
    }

    private String escapeJson(String raw) {
        return "\"" + raw.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
