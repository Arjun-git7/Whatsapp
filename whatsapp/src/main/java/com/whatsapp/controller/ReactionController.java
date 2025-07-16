package com.whatsapp.controller;

import com.whatsapp.dto.ReactRequest;
import com.whatsapp.model.*;
import com.whatsapp.repository.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
@Api(tags="Reactions")
public class ReactionController {

    private final ReactionRepository reactionRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    // Add reaction to a message
    @PostMapping
    @ApiOperation("React to a message with an emoji")
    public ResponseEntity<?> reactToMessage(@RequestBody ReactRequest request) {
        // Validate emoji type
        Emoji emoji;
        try {
            emoji = Emoji.valueOf(request.getEmoji().toLowerCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid emoji type");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = messageRepository.findById(request.getMessageId())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        Reaction reaction = Reaction.builder()
                .emoji(emoji)
                .user(user)
                .message(message)
                .build();

        reactionRepository.save(reaction);
        return ResponseEntity.ok("Reaction saved successfully");
    }

    // Get reactions for a message
    @GetMapping("/message/{messageId}")
    @ApiOperation("Get reactions for a message")
    public List<Reaction> getReactionsForMessage(@PathVariable Long messageId) {
        return reactionRepository.findByMessage_Id(messageId);
    }

    // Get all reactions by a user
    @GetMapping("/user/{userId}")
    @ApiOperation("Get all reactions by a user")
    public List<Reaction> getReactionsByUser(@PathVariable Long userId) {
        return reactionRepository.findByUser_Id(userId);
    }
}
