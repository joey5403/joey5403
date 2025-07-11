package com.joeyzheng.ai.utils;

import com.joeyzheng.ai.model.ChatResponse;
import com.joeyzheng.ai.model.Generation;
import com.joeyzheng.ai.model.AssistantMessage;
import com.joeyzheng.ai.model.ChatResponseMetadata;
import com.joeyzheng.ai.model.Usage;

import java.util.List;

import static org.mockito.Mockito.*;

public class TestDebugger {
    
    public static void main(String[] args) {
        // Create mocks
        ChatResponse mockChatResponse = mock(ChatResponse.class);
        Generation mockGeneration = mock(Generation.class);
        AssistantMessage mockAssistantMessage = mock(AssistantMessage.class);
        ChatResponseMetadata mockMetadata = mock(ChatResponseMetadata.class);
        Usage mockUsage = mock(Usage.class);

        // Setup mock relationships
        when(mockChatResponse.getResults()).thenReturn(List.of(mockGeneration));
        when(mockGeneration.getOutput()).thenReturn(mockAssistantMessage);
        when(mockChatResponse.getMetadata()).thenReturn(mockMetadata);
        when(mockMetadata.getUsage()).thenReturn(mockUsage);
        when(mockAssistantMessage.getContent()).thenReturn("Hello, world!");
        when(mockUsage.getPromptTokens()).thenReturn(10);
        when(mockUsage.getGenerationTokens()).thenReturn(15);
        when(mockUsage.getTotalTokens()).thenReturn(25);

        // Test the converter
        String result = ChatResponseToVercelStreamConverter.convertToVercelStream(mockChatResponse);
        System.out.println("Generated output:");
        System.out.println("'" + result + "'");
        System.out.println("Lines:");
        String[] lines = result.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.println("Line " + i + ": '" + lines[i] + "'");
        }
        
        // Test assertions
        System.out.println("\nTest assertions:");
        System.out.println("Contains '0:\"Hello, world!\"': " + result.contains("0:\"Hello, world!\""));
        System.out.println("Contains '8:{\"type\":\"finish\"': " + result.contains("8:{\"type\":\"finish\""));
        System.out.println("Contains '\"promptTokens\":10': " + result.contains("\"promptTokens\":10"));
        System.out.println("Contains '\"completionTokens\":15': " + result.contains("\"completionTokens\":15"));
        System.out.println("Contains '\"totalTokens\":25': " + result.contains("\"totalTokens\":25"));
    }
}