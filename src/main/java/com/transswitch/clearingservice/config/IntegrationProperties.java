package com.transswitch.clearingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integrations")
public class IntegrationProperties {

    private PaymentProcessing paymentProcessing = new PaymentProcessing();

    public IntegrationProperties() {
    }

    public PaymentProcessing getPaymentProcessing() {
        return paymentProcessing;
    }

    public void setPaymentProcessing(PaymentProcessing paymentProcessing) {
        this.paymentProcessing = paymentProcessing;
    }

    public static class PaymentProcessing {
        private String baseUrl;
        private int connectTimeoutMs;
        private int readTimeoutMs;

        public PaymentProcessing() {
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public int getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public void setConnectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
        }

        public int getReadTimeoutMs() {
            return readTimeoutMs;
        }

        public void setReadTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
        }
    }
}
