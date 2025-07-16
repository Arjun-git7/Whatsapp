package com.whatsapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateChatroomRequest {
    private String name;
    private Boolean isGroup;
    private List<Long> participantIds;
}