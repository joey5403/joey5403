package com.joeyzheng.ai.demo;

import com.joeyzheng.ai.model.*;
import com.joeyzheng.ai.utils.ChatResponseToVercelStreamConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo class showing how to use the ChatResponseToVercelStreamConverter
 */
public class ConverterDemo {
    
    public static void main(String[] args) {
        System.out.println("=== Spring AI to Vercel Stream Converter Demo ===\n");
        
        // Create a sample ChatResponse
        ChatResponse mockResponse = createSampleChatResponse();
        
        // Example 1: Basic conversion
        System.out.println("1. Basic Conversion:");
        String basicStream = ChatResponseToVercelStreamConverter.convertToVercelStream(mockResponse);
        System.out.println(basicStream);
        
        // Example 2: Chunked conversion (for streaming effect)
        System.out.println("2. Chunked Conversion (10 chars per chunk):");
        String chunkedStream = ChatResponseToVercelStreamConverter.convertToVercelStreamChunked(mockResponse, 10);
        System.out.println(chunkedStream);
        
        // Example 3: With tool calls
        System.out.println("3. With Tool Calls:");
        ChatResponse toolResponse = createSampleChatResponseWithToolCalls();
        String toolStream = ChatResponseToVercelStreamConverter.convertToVercelStreamWithToolCalls(toolResponse);
        System.out.println(toolStream);
        
        System.out.println("\n=== Demo Complete ===");
    }
    
    private static ChatResponse createSampleChatResponse() {
        return new ChatResponse() {
            @Override
            public List<Generation> getResults() {
                List<Generation> results = new ArrayList<>();
                results.add(new Generation() {
                    @Override
                    public AssistantMessage getOutput() {
                        return new AssistantMessage() {
                            @Override
                            public String getContent() {
                                return "Hello! I'm a helpful AI assistant. How can I help you today?";
                            }
                            
                            @Override
                            public List<ToolCall> getToolCalls() {
                                return null;
                            }
                        };
                    }
                });
                return results;
            }
            
            @Override
            public ChatResponseMetadata getMetadata() {
                return new ChatResponseMetadata() {
                    @Override
                    public Usage getUsage() {
                        return new Usage() {
                            @Override
                            public Integer getPromptTokens() {
                                return 12;
                            }
                            
                            @Override
                            public Integer getGenerationTokens() {
                                return 18;
                            }
                            
                            @Override
                            public Integer getTotalTokens() {
                                return 30;
                            }
                        };
                    }
                };
            }
        };
    }
    
    private static ChatResponse createSampleChatResponseWithToolCalls() {
        return new ChatResponse() {
            @Override
            public List<Generation> getResults() {
                List<Generation> results = new ArrayList<>();
                results.add(new Generation() {
                    @Override
                    public AssistantMessage getOutput() {
                        return new AssistantMessage() {
                            @Override
                            public String getContent() {
                                return "I'll help you calculate that. Let me use the calculator tool.";
                            }
                            
                            @Override
                            public List<ToolCall> getToolCalls() {
                                List<ToolCall> toolCalls = new ArrayList<>();
                                toolCalls.add(new ToolCall() {
                                    @Override
                                    public String id() {
                                        return "call_123456";
                                    }
                                    
                                    @Override
                                    public String name() {
                                        return "calculator";
                                    }
                                    
                                    @Override
                                    public String arguments() {
                                        return "{\"operation\":\"add\",\"a\":5,\"b\":3}";
                                    }
                                });
                                return toolCalls;
                            }
                        };
                    }
                });
                return results;
            }
            
            @Override
            public ChatResponseMetadata getMetadata() {
                return new ChatResponseMetadata() {
                    @Override
                    public Usage getUsage() {
                        return new Usage() {
                            @Override
                            public Integer getPromptTokens() {
                                return 20;
                            }
                            
                            @Override
                            public Integer getGenerationTokens() {
                                return 25;
                            }
                            
                            @Override
                            public Integer getTotalTokens() {
                                return 45;
                            }
                        };
                    }
                };
            }
        };
    }
}