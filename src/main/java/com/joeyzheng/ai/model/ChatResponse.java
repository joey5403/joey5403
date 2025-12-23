package com.joeyzheng.ai.model;

import java.util.List;

/**
 * Interface representing a Spring AI ChatResponse
 * This is a simplified interface to avoid Spring AI dependency issues
 */
public interface ChatResponse {
    List<Generation> getResults();
    ChatResponseMetadata getMetadata();
}