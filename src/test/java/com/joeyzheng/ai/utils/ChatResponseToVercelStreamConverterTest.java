package com.joeyzheng.ai.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import com.joeyzheng.ai.model.ChatResponse;
import com.joeyzheng.ai.model.Generation;
import com.joeyzheng.ai.model.AssistantMessage;
import com.joeyzheng.ai.model.ChatResponseMetadata;
import com.joeyzheng.ai.model.Usage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatResponseToVercelStreamConverterTest {

    private ChatResponse mockChatResponse;
    private Generation mockGeneration;
    private AssistantMessage mockAssistantMessage;
    private ChatResponseMetadata mockMetadata;
    private Usage mockUsage;

    @BeforeEach
    void setUp() {
        // Create mocks
        mockChatResponse = mock(ChatResponse.class);
        mockGeneration = mock(Generation.class);
        mockAssistantMessage = mock(AssistantMessage.class);
        mockMetadata = mock(ChatResponseMetadata.class);
        mockUsage = mock(Usage.class);

        // Setup mock relationships
        when(mockChatResponse.getResults()).thenReturn(List.of(mockGeneration));
        when(mockGeneration.getOutput()).thenReturn(mockAssistantMessage);
        when(mockChatResponse.getMetadata()).thenReturn(mockMetadata);
        when(mockMetadata.getUsage()).thenReturn(mockUsage);
    }

    @Test
    void testConvertToVercelStreamWithSimpleText() {
        // Given
        String expectedText = "Hello, how can I help you today?";
        when(mockAssistantMessage.getContent()).thenReturn(expectedText);
        when(mockUsage.getPromptTokens()).thenReturn(10);
        when(mockUsage.getGenerationTokens()).thenReturn(15);
        when(mockUsage.getTotalTokens()).thenReturn(25);

        // When
        String result = ChatResponseToVercelStreamConverter.convertToVercelStream(mockChatResponse);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("0:\"Hello, how can I help you today?\""));
        assertTrue(result.contains("8:") && result.contains("\"type\":\"finish\""));
        assertTrue(result.contains("\"promptTokens\":10"));
        assertTrue(result.contains("\"completionTokens\":15"));
        assertTrue(result.contains("\"totalTokens\":25"));
    }

    @Test
    void testConvertToVercelStreamWithEmptyText() {
        // Given
        when(mockAssistantMessage.getContent()).thenReturn("");
        when(mockUsage.getPromptTokens()).thenReturn(5);
        when(mockUsage.getGenerationTokens()).thenReturn(0);
        when(mockUsage.getTotalTokens()).thenReturn(5);

        // When
        String result = ChatResponseToVercelStreamConverter.convertToVercelStream(mockChatResponse);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("0:"));
        assertTrue(result.contains("8:") && result.contains("\"type\":\"finish\""));
    }

    @Test
    void testConvertToVercelStreamWithNullText() {
        // Given
        when(mockAssistantMessage.getContent()).thenReturn(null);
        when(mockUsage.getPromptTokens()).thenReturn(5);
        when(mockUsage.getGenerationTokens()).thenReturn(0);
        when(mockUsage.getTotalTokens()).thenReturn(5);

        // When
        String result = ChatResponseToVercelStreamConverter.convertToVercelStream(mockChatResponse);

        // Then
        assertNotNull(result);
        assertFalse(result.contains("0:"));
        assertTrue(result.contains("8:") && result.contains("\"type\":\"finish\""));
    }

    @Test
    void testConvertToVercelStreamChunked() {
        // Given
        String longText = "This is a long text that should be chunked into smaller pieces for streaming.";
        when(mockAssistantMessage.getContent()).thenReturn(longText);
        when(mockUsage.getPromptTokens()).thenReturn(20);
        when(mockUsage.getGenerationTokens()).thenReturn(30);
        when(mockUsage.getTotalTokens()).thenReturn(50);

        // When
        String result = ChatResponseToVercelStreamConverter.convertToVercelStreamChunked(mockChatResponse, 10);

        // Then
        assertNotNull(result);
        // Should contain multiple chunks
        long chunkCount = result.lines().filter(line -> line.startsWith("0:")).count();
        assertTrue(chunkCount > 1);
        assertTrue(result.contains("8:") && result.contains("\"type\":\"finish\""));
    }

    @Test
    void testConvertToVercelStreamWithSpecialCharacters() {
        // Given
        String textWithSpecialChars = "Hello \"world\"!\nThis is a new line.\rAnd carriage return.";
        when(mockAssistantMessage.getContent()).thenReturn(textWithSpecialChars);
        when(mockUsage.getPromptTokens()).thenReturn(15);
        when(mockUsage.getGenerationTokens()).thenReturn(20);
        when(mockUsage.getTotalTokens()).thenReturn(35);

        // When
        String result = ChatResponseToVercelStreamConverter.convertToVercelStream(mockChatResponse);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("0:"));
        // Should properly escape special characters
        assertTrue(result.contains("\\\""));
        assertTrue(result.contains("\\n"));
        assertTrue(result.contains("8:") && result.contains("\"type\":\"finish\""));
    }

    @Test
    void testConvertToVercelStreamWithNullUsage() {
        // Given
        when(mockAssistantMessage.getContent()).thenReturn("Test message");
        when(mockMetadata.getUsage()).thenReturn(null);

        // When
        String result = ChatResponseToVercelStreamConverter.convertToVercelStream(mockChatResponse);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("0:\"Test message\""));
        // Should not contain finish event when usage is null
        assertFalse(result.contains("8:"));
    }

    @Test
    void testConvertToVercelStreamWithMultipleGenerations() {
        // Given
        Generation mockGeneration2 = mock(Generation.class);
        AssistantMessage mockAssistantMessage2 = mock(AssistantMessage.class);
        
        when(mockChatResponse.getResults()).thenReturn(List.of(mockGeneration, mockGeneration2));
        when(mockGeneration.getOutput()).thenReturn(mockAssistantMessage);
        when(mockGeneration2.getOutput()).thenReturn(mockAssistantMessage2);
        when(mockAssistantMessage.getContent()).thenReturn("First message");
        when(mockAssistantMessage2.getContent()).thenReturn("Second message");
        when(mockUsage.getPromptTokens()).thenReturn(10);
        when(mockUsage.getGenerationTokens()).thenReturn(20);
        when(mockUsage.getTotalTokens()).thenReturn(30);

        // When
        String result = ChatResponseToVercelStreamConverter.convertToVercelStream(mockChatResponse);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("0:\"First message\""));
        assertTrue(result.contains("0:\"Second message\""));
        assertTrue(result.contains("8:") && result.contains("\"type\":\"finish\""));
    }
}