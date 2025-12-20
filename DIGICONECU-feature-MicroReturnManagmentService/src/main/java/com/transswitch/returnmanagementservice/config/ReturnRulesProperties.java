package com.transswitch.returnmanagementservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "returns")
public class ReturnRulesProperties {

    private int maxHours = 48;
    private boolean enforceAmountMatch = true;
    private boolean adjustBalances = false;

    public ReturnRulesProperties() {
    }

    public int getMaxHours() {
        return maxHours;
    }

    public void setMaxHours(int maxHours) {
        this.maxHours = maxHours;
    }

    public boolean isEnforceAmountMatch() {
        return enforceAmountMatch;
    }

    public void setEnforceAmountMatch(boolean enforceAmountMatch) {
        this.enforceAmountMatch = enforceAmountMatch;
    }

    public boolean isAdjustBalances() {
        return adjustBalances;
    }

    public void setAdjustBalances(boolean adjustBalances) {
        this.adjustBalances = adjustBalances;
    }
}
