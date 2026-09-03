package com.AI.AILEAD.DTO;


public class ChatResponse {

    private String conversationId;
    private String message;

    public ChatResponse(String conversationId, String message) {
        this.conversationId = conversationId;
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getMessage() {
        return message;
    }
}
