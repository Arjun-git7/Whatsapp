package com.whatsapp.websocket;

import com.whatsapp.service.TypingTrackerService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final TypingTrackerService typingTrackerService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = extractUserIdFromSession(event.getSessionId());
        if (userId != null) {
            typingTrackerService.handleDisconnect(userId);
        }
    }

    private Long extractUserIdFromSession(String sessionId) {
        // Implement this if you associate sessionId <-> userId
        // Option 1: Use a WebSocketInterceptor to store user info in session
        // Option 2: Use a WebSocketPrincipal if you support Spring Security
        return null; // placeholder until implemented
    }
}
