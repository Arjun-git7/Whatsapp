package com.whatsapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private Long id;
    private String content;
    private Long senderId;
    private String senderName;
    private Long chatroomId;
    private String attachmentPath;
    private String attachmentType;
    private LocalDateTime timestamp;
    private Boolean isForwarded;
    private Long forwardedFromChatroomId;
    private LocalDateTime editedAt;
    private boolean isEdited;


}