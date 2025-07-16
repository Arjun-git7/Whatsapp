package com.whatsapp.dto;

import lombok.Data;

@Data
public class EditMessageRequest {
    private Long userId;
    private String newContent;
}
