package com.transswitch.returnmanagementservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integrations")
public class IntegrationProperties {

    private ServiceCfg paymentProcessing = new ServiceCfg();
    private ServiceCfg accountBalance = new ServiceCfg();

    public IntegrationProperties() {
    }

    public ServiceCfg getPaymentProcessing() {
        return paymentProcessing;
    }

    public void setPaymentProcessing(ServiceCfg paymentProcessing) {
        this.paymentProcessing = paymentProcessing;
    }

    public ServiceCfg getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(ServiceCfg accountBalance) {
        this.accountBalance = accountBalance;
    }

    public static class ServiceCfg {
        private String baseUrl;
        private int connectTimeoutMs;
        private int readTimeoutMs;

        public ServiceCfg() {
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
