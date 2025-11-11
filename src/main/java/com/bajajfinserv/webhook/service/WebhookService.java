package com.bajajfinserv.webhook.service;

import com.bajajfinserv.webhook.model.SolutionRequest;
import com.bajajfinserv.webhook.model.WebhookRequest;
import com.bajajfinserv.webhook.model.WebhookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WebhookService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);

    private final RestTemplate restTemplate;

    @Value("${app.user.name}")
    private String userName;

    @Value("${app.user.regNo}")
    private String regNo;

    @Value("${app.user.email}")
    private String email;

    @Value("${app.api.base-url}")
    private String baseUrl;

    @Value("${app.api.generate-webhook}")
    private String generateWebhookPath;

    @Value("${app.api.test-webhook}")
    private String testWebhookPath;

    public WebhookService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            logger.info("Starting Bajaj Finserv Webhook Qualifier Application...");
            
            // Step 1: Generate webhook
            logger.info("Step 1: Generating webhook...");
            WebhookResponse webhookResponse = generateWebhook();
            logger.info("Webhook generated successfully!");
            logger.info("Webhook URL: {}", webhookResponse.getWebhook());
            logger.info("Access Token: {}...", webhookResponse.getAccessToken().substring(0, Math.min(20, webhookResponse.getAccessToken().length())));

            // Step 2: Get the SQL solution
            logger.info("Registration Number: {}", regNo);
            String sqlQuery = getSqlSolution();
            logger.info("SQL Solution prepared");

            // Step 4: Submit the solution
            logger.info("Step 2: Submitting solution to webhook...");
            submitSolution(webhookResponse.getWebhook(), webhookResponse.getAccessToken(), sqlQuery);
            logger.info("Solution submitted successfully!");
            
            logger.info("Application completed successfully!");
        } catch (Exception e) {
            logger.error("Error during execution: {}", e.getMessage(), e);
        }
    }

    private WebhookResponse generateWebhook() {
        String url = baseUrl + generateWebhookPath;
        
        WebhookRequest request = new WebhookRequest(userName, regNo, email);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<WebhookRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<WebhookResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                WebhookResponse.class
        );
        
        return response.getBody();
    }

    private String getSqlSolution() {
        // SQL query solution for Question 2 (Even)
        return "WITH EmployeeAgeRank AS (SELECT EMP_ID, RANK() OVER(PARTITION BY DEPARTMENT_ID ORDER BY DOB DESC) as age_rank FROM EMPLOYEE) SELECT e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME, er.age_rank - 1 AS YOUNGER_EMPLOYEES_COUNT FROM EMPLOYEE e JOIN DEPARTMENT d ON e.DEPARTMENT_ID = d.DEPARTMENT_ID JOIN EmployeeAgeRank er ON e.EMP_ID = er.EMP_ID ORDER BY e.EMP_ID DESC;";
    }

    private void submitSolution(String webhookUrl, String accessToken, String sqlQuery) {
        SolutionRequest solutionRequest = new SolutionRequest(sqlQuery);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", accessToken);
        
        HttpEntity<SolutionRequest> entity = new HttpEntity<>(solutionRequest, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                entity,
                String.class
        );
        
        logger.info("Response Status: {}", response.getStatusCode());
        logger.info("Response Body: {}", response.getBody());
    }
}
