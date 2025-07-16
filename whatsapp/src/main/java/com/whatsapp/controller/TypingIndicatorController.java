package com.whatsapp.controller;

import com.whatsapp.dto.TypingIndicator;
import com.whatsapp.model.User;
import com.whatsapp.repository.UserRepository;
import com.whatsapp.service.TypingTrackerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
//import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@Api(tags = "Typing Indicator")
public class TypingIndicatorController {

   private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    private final TypingTrackerService typingTrackerService;

    private final Map<String, Instant> typingCache = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    {
        scheduler.initialize(); // Init scheduler during construction
    }

//    @MessageMapping("/typing")
//    public void sendTypingIndicator(@Payload TypingIndicator typing) {
//        User user = userRepository.findById(typing.getUserId())
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (typing.isTyping()) {
//            typingTrackerService.userStartedTyping(
//                    typing.getChatroomId(),
//                    typing.getUserId(),
//                    user.getDisplayName()
//            );
//        } else {
//            typingTrackerService.userStoppedTyping(
//                    typing.getChatroomId(),
//                    typing.getUserId(),
//                    user.getDisplayName()
//            );
//        }
//    }

    // Shows typing by user
    @PostMapping("/start")
    @ApiOperation("User is Typing indicator")
    public ResponseEntity<?> startTyping(@RequestParam Long userId, @RequestParam Long chatroomId) {
        String key = chatroomId + ":" + userId;
        typingCache.put(key, Instant.now());

        // Notify all users in the chatroom
        messagingTemplate.convertAndSend("/topic/typing/" + chatroomId,
                Map.of("userId", userId, "typing", true));

        // Auto-remove after 5 seconds of inactivity
        scheduler.schedule(() -> {
            Instant lastTyped = typingCache.get(key);
            if (lastTyped != null && Instant.now().minusSeconds(5).isAfter(lastTyped)) {
                typingCache.remove(key);
                messagingTemplate.convertAndSend("/topic/typing/" + chatroomId,
                        Map.of("userId", userId, "typing", false));
            }
        }, Instant.now().plusSeconds(5));

        return ResponseEntity.ok("User is typing...");
    }

    //Stops typing
    @PostMapping("/stop")
    @ApiOperation("Typing stop indicator")
    public ResponseEntity<?> stopTyping(@RequestParam Long userId, @RequestParam Long chatroomId) {
        String key = chatroomId + ":" + userId;
        typingCache.remove(key);

        messagingTemplate.convertAndSend("/topic/typing/" + chatroomId,
                Map.of("userId", userId, "typing", false));

        return ResponseEntity.ok("User stopped typing.");
    }
}
