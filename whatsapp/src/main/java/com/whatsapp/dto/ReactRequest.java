package com.whatsapp.dto;

import lombok.Data;

@Data
public class ReactRequest {
    private Long userId;
    private Long messageId;
    private String emoji; // must be one of: thumbup, love, crying, surprised
}
