package com.whatsapp.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long senderId;
    private Long chatroomId;
    private String content;
}