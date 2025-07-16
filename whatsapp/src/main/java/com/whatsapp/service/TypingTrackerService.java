package com.whatsapp.service;

import com.whatsapp.dto.TypingIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class TypingTrackerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Instant> typingCache = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> timeoutTasks = new ConcurrentHashMap<>();

    private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    {
        scheduler.initialize();
    }

    public void userStartedTyping(Long chatroomId, Long userId, String displayName) {
        String key = chatroomId + "_" + userId;
        typingCache.put(key, Instant.now());

        // Broadcast typing = true
        messagingTemplate.convertAndSend("/topic/chatroom/" + chatroomId + "/typing",
                Map.of("userId", userId, "displayName", displayName, "typing", true));

        // Cancel previous task if exists
        ScheduledFuture<?> oldTask = timeoutTasks.get(key);
        if (oldTask != null) oldTask.cancel(true);

        ScheduledFuture<?> newTask = scheduler.schedule(() -> {
            typingCache.remove(key);
            Instant lastTyped = typingCache.get(key);
            if (lastTyped != null && Instant.now().minusSeconds(5).isAfter(lastTyped)) {
                messagingTemplate.convertAndSend("/topic/chatroom/" + chatroomId + "/typing",
                        Map.of("userId", userId, "displayName", displayName, "typing", false));
                timeoutTasks.remove(key);
            }
        }, Instant.now().plusSeconds(5));

        // Schedule auto-reset after 5 seconds
//        scheduler.schedule(() -> {
//            Instant lastTyped = typingCache.get(key);
//            if (lastTyped != null && Instant.now().minusSeconds(5).isAfter(lastTyped)) {
//                typingCache.remove(key);
//                messagingTemplate.convertAndSend("/topic/chatroom/" + chatroomId + "/typing",
//                        Map.of("userId", userId, "displayName", displayName, "typing", false));
//            }
//        }, Instant.now().plusSeconds(5));

        timeoutTasks.put(key, newTask);
    }

    public void userStoppedTyping(Long chatroomId, Long userId, String displayName) {
        String key = chatroomId + ":" + userId;
        typingCache.remove(key);

        ScheduledFuture<?> task = timeoutTasks.remove(key);
        if (task != null) task.cancel(true);

        messagingTemplate.convertAndSend("/topic/chatroom/" + chatroomId + "/typing",
                Map.of("userId", userId, "displayName", displayName, "typing", false));
    }

    public void handleDisconnect(Long userId) {
        // Optionally iterate through all keys and clear typing for disconnected user
        List<String> keys = timeoutTasks.keySet().stream()
                .filter(k -> k.endsWith("_" + userId))
                .toList();

        for (String key : keys) {
            String[] parts = key.split("_");
            Long chatroomId = Long.parseLong(parts[0]);

            TypingIndicator indicator = new TypingIndicator(chatroomId, userId, false, "");
            messagingTemplate.convertAndSend("/topic/chatroom/" + chatroomId + "/typing", indicator);

            ScheduledFuture<?> task = timeoutTasks.remove(key);
            if (task != null) task.cancel(true);
        }
    }
}