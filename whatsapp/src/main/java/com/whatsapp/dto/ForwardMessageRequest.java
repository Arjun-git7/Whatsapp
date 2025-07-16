package com.whatsapp.dto;

import lombok.Data;

@Data
public class ForwardMessageRequest {

    private Long senderId;
    private Long targetChatroomId;
}
