package com.joeyzheng.ai.model;

/**
 * Interface representing a Spring AI ToolCall
 */
public interface ToolCall {
    String id();
    String name();
    String arguments();
}