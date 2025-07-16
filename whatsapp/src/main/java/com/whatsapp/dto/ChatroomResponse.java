package com.whatsapp.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ChatroomResponse {
    private Long id;
    private String name;
    private Boolean isGroup;
    private LocalDateTime createdAt;
    private List<UserSummary> participants;
}
