package com.whatsapp.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String displayName;
    private String email;
    private String password;
    private String status;
    private String profilePicture;
}