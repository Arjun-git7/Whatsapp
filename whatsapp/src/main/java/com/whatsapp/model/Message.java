package com.whatsapp.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @CreationTimestamp
    private LocalDateTime timestamp;

    @Column(nullable = true)
    private String attachmentPath;

    @Column(nullable = true)
    private String attachmentType;

    @Column(nullable = false)
    private Boolean isRead = false;

    @Column(nullable = false)
    private Boolean isDelivered = false;

    @Column(name = "is_forwarded")
    private Boolean isForwarded = false;

    @ManyToOne
    @JoinColumn(name = "forwarded_from_chatroom_id")
    private Chatroom forwardedFromChatroom;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;
}