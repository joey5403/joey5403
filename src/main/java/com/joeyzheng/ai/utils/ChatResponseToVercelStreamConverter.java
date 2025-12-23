package com.joeyzheng.ai.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joeyzheng.ai.model.ChatResponse;
import com.joeyzheng.ai.model.Generation;
import com.joeyzheng.ai.model.Usage;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to convert Spring AI ChatResponse to x-vercel-ai-data-stream format
 * Based on Vercel AI SDK stream protocol: https://ai-sdk.dev/docs/ai-sdk-ui/stream-protocol
 * 
 * This utility works with any ChatResponse implementation that follows the Spring AI pattern.
 * The interfaces used here are compatible with org.springframework.ai.chat.model.ChatResponse
 * and related classes from Spring AI framework.
 */
public class ChatResponseToVercelStreamConverter {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Convert ChatResponse to x-vercel-ai-data-stream format
     * @param chatResponse Spring AI ChatResponse
     * @return x-vercel-ai-data-stream formatted string
     */
    public static String convertToVercelStream(ChatResponse chatResponse) {
        StringBuilder streamBuilder = new StringBuilder();
        
        // Process each generation in the response
        for (Generation generation : chatResponse.getResults()) {
            // Add text content chunk (type 0)
            String textContent = generation.getOutput().getContent();
            if (textContent != null && !textContent.isEmpty()) {
                streamBuilder.append("0:").append(escapeJsonString(textContent)).append("\n");
            }
        }
        
        // Add finish event with usage information (type 8)
        Usage usage = chatResponse.getMetadata().getUsage();
        if (usage != null) {
            Map<String, Object> finishEvent = new HashMap<>();
            finishEvent.put("type", "finish");
            
            Map<String, Object> usageMap = new HashMap<>();
            usageMap.put("promptTokens", usage.getPromptTokens());
            usageMap.put("completionTokens", usage.getGenerationTokens());
            usageMap.put("totalTokens", usage.getTotalTokens());
            
            finishEvent.put("usage", usageMap);
            
            try {
                String finishJson = objectMapper.writeValueAsString(finishEvent);
                streamBuilder.append("8:").append(finishJson).append("\n");
            } catch (JsonProcessingException e) {
                // Handle JSON serialization error
                streamBuilder.append("8:{\"type\":\"finish\",\"error\":\"Failed to serialize usage data\"}\n");
            }
        }
        
        return streamBuilder.toString();
    }
    
    /**
     * Convert ChatResponse to x-vercel-ai-data-stream format with streaming text chunks
     * @param chatResponse Spring AI ChatResponse
     * @param chunkSize size of each text chunk for streaming
     * @return x-vercel-ai-data-stream formatted string with chunked text
     */
    public static String convertToVercelStreamChunked(ChatResponse chatResponse, int chunkSize) {
        StringBuilder streamBuilder = new StringBuilder();
        
        // Process each generation in the response
        for (Generation generation : chatResponse.getResults()) {
            String textContent = generation.getOutput().getContent();
            if (textContent != null && !textContent.isEmpty()) {
                // Split text into chunks for streaming effect
                for (int i = 0; i < textContent.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, textContent.length());
                    String chunk = textContent.substring(i, end);
                    streamBuilder.append("0:").append(escapeJsonString(chunk)).append("\n");
                }
            }
        }
        
        // Add finish event with usage information (type 8)
        Usage usage = chatResponse.getMetadata().getUsage();
        if (usage != null) {
            Map<String, Object> finishEvent = new HashMap<>();
            finishEvent.put("type", "finish");
            
            Map<String, Object> usageMap = new HashMap<>();
            usageMap.put("promptTokens", usage.getPromptTokens());
            usageMap.put("completionTokens", usage.getGenerationTokens());
            usageMap.put("totalTokens", usage.getTotalTokens());
            
            finishEvent.put("usage", usageMap);
            
            try {
                String finishJson = objectMapper.writeValueAsString(finishEvent);
                streamBuilder.append("8:").append(finishJson).append("\n");
            } catch (JsonProcessingException e) {
                // Handle JSON serialization error
                streamBuilder.append("8:{\"type\":\"finish\",\"error\":\"Failed to serialize usage data\"}\n");
            }
        }
        
        return streamBuilder.toString();
    }
    
    /**
     * Convert ChatResponse to x-vercel-ai-data-stream format for tool calls
     * @param chatResponse Spring AI ChatResponse
     * @return x-vercel-ai-data-stream formatted string with tool call support
     */
    public static String convertToVercelStreamWithToolCalls(ChatResponse chatResponse) {
        StringBuilder streamBuilder = new StringBuilder();
        
        // Process each generation in the response
        for (Generation generation : chatResponse.getResults()) {
            String textContent = generation.getOutput().getContent();
            
            // Add text content chunk (type 0)
            if (textContent != null && !textContent.isEmpty()) {
                streamBuilder.append("0:").append(escapeJsonString(textContent)).append("\n");
            }
            
            // Check for tool calls in the generation
            if (generation.getOutput().getToolCalls() != null && !generation.getOutput().getToolCalls().isEmpty()) {
                generation.getOutput().getToolCalls().forEach(toolCall -> {
                    Map<String, Object> toolCallEvent = new HashMap<>();
                    toolCallEvent.put("type", "tool_call");
                    toolCallEvent.put("id", toolCall.id());
                    
                    Map<String, Object> function = new HashMap<>();
                    function.put("name", toolCall.name());
                    function.put("arguments", toolCall.arguments());
                    
                    toolCallEvent.put("function", function);
                    
                    try {
                        String toolCallJson = objectMapper.writeValueAsString(toolCallEvent);
                        streamBuilder.append("1:").append(toolCallJson).append("\n");
                    } catch (JsonProcessingException e) {
                        // Handle JSON serialization error
                        streamBuilder.append("1:{\"type\":\"tool_call\",\"error\":\"Failed to serialize tool call\"}\n");
                    }
                });
            }
        }
        
        // Add finish event with usage information (type 8)
        Usage usage = chatResponse.getMetadata().getUsage();
        if (usage != null) {
            Map<String, Object> finishEvent = new HashMap<>();
            finishEvent.put("type", "finish");
            
            Map<String, Object> usageMap = new HashMap<>();
            usageMap.put("promptTokens", usage.getPromptTokens());
            usageMap.put("completionTokens", usage.getGenerationTokens());
            usageMap.put("totalTokens", usage.getTotalTokens());
            
            finishEvent.put("usage", usageMap);
            
            try {
                String finishJson = objectMapper.writeValueAsString(finishEvent);
                streamBuilder.append("8:").append(finishJson).append("\n");
            } catch (JsonProcessingException e) {
                // Handle JSON serialization error
                streamBuilder.append("8:{\"type\":\"finish\",\"error\":\"Failed to serialize usage data\"}\n");
            }
        }
        
        return streamBuilder.toString();
    }
    
    /**
     * Escape JSON string to prevent JSON parsing issues
     * @param text raw text to escape
     * @return escaped JSON string
     */
    private static String escapeJsonString(String text) {
        try {
            return objectMapper.writeValueAsString(text);
        } catch (JsonProcessingException e) {
            // Fallback to manual escaping
            return "\"" + text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
        }
    }
}