package com.codingplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Judge Service Configuration.
 */
@Configuration
public class JudgeConfig {

    @Value("${judge.service.host:localhost}")
    private String judgeHost;

    @Value("${judge.service.port:5000}")
    private int judgePort;

    @Value("${judge.service.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${judge.service.timeout.read:30000}")
    private int readTimeout;

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return new RestTemplate(factory);
    }

    public String getJudgeHost() {
        return judgeHost;
    }

    public int getJudgePort() {
        return judgePort;
    }

    public String getJudgeBaseUrl() {
        return String.format("http://%s:%d", judgeHost, judgePort);
    }
}

