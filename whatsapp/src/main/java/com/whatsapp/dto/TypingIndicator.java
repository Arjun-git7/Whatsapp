package com.whatsapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TypingIndicator {
    private Long chatroomId;
    private Long userId;
    private boolean isTyping;
    private String displayName;
}
