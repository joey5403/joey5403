# Spring AI to Vercel Stream Converter

A Java utility class that converts Spring AI's `ChatResponse` to the x-vercel-ai-data-stream format used by Vercel AI SDK.

## Features

- **Simple Conversion**: Convert Spring AI ChatResponse to Vercel stream format
- **Chunked Streaming**: Support for text chunking to simulate streaming behavior
- **Tool Call Support**: Handle tool calls in the stream format
- **Proper JSON Escaping**: Safe handling of special characters
- **Usage Tracking**: Include token usage information in finish events

## Stream Protocol

This converter implements the [Vercel AI SDK stream protocol](https://ai-sdk.dev/docs/ai-sdk-ui/stream-protocol) with the following event types:

- `0: "text"` - Text content chunks
- `1: {...}` - Tool call events
- `2: {...}` - Tool result events  
- `8: {...}` - Finish events with usage information

## Usage

### Basic Conversion

```java
import com.joeyzheng.ai.utils.ChatResponseToVercelStreamConverter;
import org.springframework.ai.chat.model.ChatResponse;

// Assuming you have a ChatResponse from Spring AI
ChatResponse chatResponse = chatClient.call(prompt);

// Convert to Vercel stream format
String vercelStream = ChatResponseToVercelStreamConverter.convertToVercelStream(chatResponse);

// Output example:
// 0:"Hello! How can I help you today?"
// 8:{"type":"finish","usage":{"promptTokens":10,"completionTokens":15,"totalTokens":25}}
```

### Chunked Streaming

For simulating real-time streaming behavior:

```java
// Convert with text chunking (10 characters per chunk)
String chunkedStream = ChatResponseToVercelStreamConverter.convertToVercelStreamChunked(chatResponse, 10);

// Output example:
// 0:"Hello! How"
// 0:" can I hel"
// 0:"p you toda"
// 0:"y?"
// 8:{"type":"finish","usage":{"promptTokens":10,"completionTokens":15,"totalTokens":25}}
```

### Tool Call Support

For responses that include tool calls:

```java
String streamWithTools = ChatResponseToVercelStreamConverter.convertToVercelStreamWithToolCalls(chatResponse);

// Output example:
// 0:"I'll help you with that calculation."
// 1:{"type":"tool_call","id":"call_123","function":{"name":"calculator","arguments":"{\"operation\":\"add\",\"a\":5,\"b\":3}"}}
// 8:{"type":"finish","usage":{"promptTokens":20,"completionTokens":25,"totalTokens":45}}
```

### HTTP Response Integration

Using with Spring WebFlux for streaming responses:

```java
@RestController
public class ChatController {
    
    @Autowired
    private ChatClient chatClient;
    
    @PostMapping(value = "/chat", produces = "text/plain; charset=utf-8")
    public ResponseEntity<String> chat(@RequestBody String message) {
        try {
            ChatResponse response = chatClient.call(new Prompt(message));
            String vercelStream = ChatResponseToVercelStreamConverter.convertToVercelStream(response);
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/plain; charset=utf-8")
                .header("X-Content-Type-Options", "nosniff")
                .body(vercelStream);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing request");
        }
    }
}
```

## Dependencies

Add these dependencies to your `pom.xml`:

```xml
<dependencies>
    <!-- Spring AI -->
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-core</artifactId>
        <version>1.0.0-M3</version>
    </dependency>

    <!-- Jackson for JSON processing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.15.2</version>
    </dependency>
</dependencies>
```

## Testing

Run the tests to verify functionality:

```bash
mvn test
```

## Stream Format Examples

### Text Content
```
0:"Hello, how can I help you today?"
```

### Tool Call
```
1:{"type":"tool_call","id":"call_abc123","function":{"name":"get_weather","arguments":"{\"location\":\"New York\"}"}}
```

### Finish Event
```
8:{"type":"finish","usage":{"promptTokens":12,"completionTokens":8,"totalTokens":20}}
```

## Error Handling

The converter includes robust error handling:

- **JSON Serialization Errors**: Falls back to manual escaping if Jackson fails
- **Null Safety**: Handles null content and usage information gracefully
- **Empty Content**: Skips empty or null text content
- **Missing Usage**: Omits finish event if usage information is unavailable

## Integration with Frontend

Use with Vercel AI SDK on the frontend:

```typescript
import { useChat } from 'ai/react';

const { messages, input, handleInputChange, handleSubmit } = useChat({
  api: '/api/chat', // Your Spring Boot endpoint
  headers: {
    'Content-Type': 'application/json',
  },
});
```

## License

This utility is provided as-is for educational and development purposes.