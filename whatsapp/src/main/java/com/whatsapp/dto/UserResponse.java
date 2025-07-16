package com.whatsapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String profilePicture;
    private String status;
}