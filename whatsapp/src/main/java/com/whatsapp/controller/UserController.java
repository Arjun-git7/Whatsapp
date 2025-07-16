package com.whatsapp.controller;

import com.whatsapp.dto.CreateUserRequest;
import com.whatsapp.dto.LoginRequest;
import com.whatsapp.dto.UserResponse;
import com.whatsapp.model.User;
import com.whatsapp.repository.UserRepository;
import com.whatsapp.service.FileStorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Api(tags = "User Profile")
public class UserController {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final FileStorageService fileStorageService;

    //LOGIN USER
    @PostMapping("/login")
    @ApiOperation("Login of user")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        return ResponseEntity.ok(toResponse(user));
    }
    // CREATE USER
    @PostMapping
    @ApiOperation("Register a new user")
    public UserResponse createUser(@RequestBody CreateUserRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .displayName(request.getDisplayName())
                .status(request.getStatus())
                .build();

        user = userRepository.save(user);

        return toResponse(user);
    }

    // GET USERS WITH PAGINATION
    @GetMapping
    @ApiOperation("List of user (pagination)")
    public Page<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable);
    }

    // GET USER BY ID
    @GetMapping("/{id}")
    @ApiOperation("Getting user by ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(toResponse(user));
    }

    // UPDATE USER
    @PutMapping("/{id}")
    @ApiOperation("Updating user details")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setDisplayName(updatedUser.getDisplayName());
            user.setStatus(updatedUser.getStatus());
            return userRepository.save(user);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // DELETE USER
    @DeleteMapping("/{id}")
    @ApiOperation("Delete user by ID")
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .status(user.getStatus())
                .build();
    }

    // Update Display name and Profile picture
    @PutMapping(value = "/{id}/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("Update user display name and profile picture")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @RequestParam String displayName,
            @RequestParam(required = false) MultipartFile profilePicture
    ) throws IOException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDisplayName(displayName);

        if (profilePicture != null && !profilePicture.isEmpty()) {
            String filePath = fileStorageService.saveFile(profilePicture, "profile");
            user.setProfilePicture(filePath);
        }

        userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

}