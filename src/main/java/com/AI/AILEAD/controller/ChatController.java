package com.AI.AILEAD.controller;


import com.AI.AILEAD.DTO.ChatRequest;
import com.AI.AILEAD.DTO.ChatResponse;
import com.AI.AILEAD.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String response = chatService.chat(
                request.getMessage(),
                request.getConversationId()
        );

        return new ChatResponse(
                request.getConversationId(),
                response
        );
    }
}
