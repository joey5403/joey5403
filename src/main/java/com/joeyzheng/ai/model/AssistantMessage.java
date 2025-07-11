package com.joeyzheng.ai.model;

import java.util.List;

/**
 * Interface representing a Spring AI AssistantMessage
 */
public interface AssistantMessage {
    String getContent();
    List<ToolCall> getToolCalls();
}