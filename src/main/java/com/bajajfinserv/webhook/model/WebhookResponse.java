package com.bajajfinserv.webhook.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebhookResponse {
    private String webhook;
    private String accessToken;
}
