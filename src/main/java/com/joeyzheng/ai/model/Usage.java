package com.joeyzheng.ai.model;

/**
 * Interface representing Spring AI Usage
 */
public interface Usage {
    Integer getPromptTokens();
    Integer getGenerationTokens();
    Integer getTotalTokens();
}