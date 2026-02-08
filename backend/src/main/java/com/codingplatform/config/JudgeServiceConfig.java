package com.codingplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for multi-language Judge Services.
 * 
 * Routes submissions to the appropriate judge based on language:
 * - Python → port 5000
 * - C++    → port 5002
 * - Java   → port 5003
 * - JS     → port 5004
 */
@Configuration
@ConfigurationProperties(prefix = "judge.service")
public class JudgeServiceConfig {

    private String host = "localhost";
    
    // Port mapping for each language
    private Map<String, Integer> ports = new HashMap<>() {{
        put("python", 5000);
        put("cpp", 5002);
        put("java", 5003);
        put("js", 5004);
    }};

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<String, Integer> getPorts() {
        return ports;
    }

    public void setPorts(Map<String, Integer> ports) {
        this.ports = ports;
    }

    /**
     * Get the base URL for a specific language judge.
     */
    public String getJudgeUrl(String language) {
        Integer port = ports.get(language.toLowerCase());
        if (port == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }
        return "http://" + host + ":" + port;
    }

    /**
     * Get the /judge endpoint for a specific language.
     */
    public String getJudgeEndpoint(String language) {
        return getJudgeUrl(language) + "/judge";
    }

    /**
     * Get the /health endpoint for a specific language.
     */
    public String getHealthEndpoint(String language) {
        return getJudgeUrl(language) + "/health";
    }

    /**
     * Get the /problem endpoint (uses Python judge by default).
     */
    public String getProblemEndpoint() {
        return getJudgeUrl("python") + "/problem";
    }

    /**
     * Get list of supported languages.
     */
    public java.util.Set<String> getSupportedLanguages() {
        return ports.keySet();
    }
}

