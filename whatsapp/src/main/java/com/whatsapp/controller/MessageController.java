package com.whatsapp.controller;

import com.whatsapp.dto.EditMessageRequest;
import com.whatsapp.dto.ForwardMessageRequest;
import com.whatsapp.dto.MessageResponse;
import com.whatsapp.dto.SendMessageRequest;
import com.whatsapp.model.Chatroom;
import com.whatsapp.model.Message;
import com.whatsapp.model.User;
import com.whatsapp.repository.ChatroomRepository;
import com.whatsapp.repository.MessageRepository;
import com.whatsapp.repository.UserRepository;
import com.whatsapp.service.FileStorageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
@Api(tags = "Messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatroomRepository chatroomRepository;
    private final FileStorageService fileStorageService;

    private final SimpMessagingTemplate messagingTemplate;

    //  Send Text Message
    @PostMapping
    @ApiOperation("Sending a Text Message")
    public MessageResponse sendTextMessage(@RequestBody SendMessageRequest request) {
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Chatroom chatroom = chatroomRepository.findById(request.getChatroomId())
                .orElseThrow(() -> new RuntimeException("Chatroom not found"));

        Message message = Message.builder()
                .sender(sender)
                .chatroom(chatroom)
                .content(request.getContent())
                .isDelivered(false)
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .build();

        //return toResponse(messageRepository.save(message));
        Message saved = messageRepository.save(message);
        MessageResponse response = toResponse(saved);

//  Push notification to subscribers of this chatroom
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroom.getId(),
                response
        );

        return response;
    }

    //  Send Attachment (Image/Video)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("Sending Attachment (Image/Video)")
    public MessageResponse sendAttachment(
            @RequestParam Long senderId,
            @RequestParam Long chatroomId,
            @RequestParam MultipartFile file
    ) throws IOException {

        String contentType = file.getContentType();
        String type;

        if (contentType == null) {
            throw new IllegalArgumentException("Invalid file type");
        } else if (contentType.startsWith("image")) {
            type = "image";
        } else if (contentType.startsWith("video")) {
            type = "video";
        } else {
            throw new IllegalArgumentException("Only image or video files are allowed");
        }

        String filePath = fileStorageService.saveFile(file, type);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new RuntimeException("Chatroom not found"));

        Message message = Message.builder()
                .sender(sender)
                .chatroom(chatroom)
                .attachmentPath(filePath)
                .attachmentType(type)
                .build();

       // return toResponse(messageRepository.save(message));
        Message saved = messageRepository.save(message);
        MessageResponse response = toResponse(saved);

//  Push notification
        messagingTemplate.convertAndSend(
                "/topic/chatroom/" + chatroom.getId(),
                response
        );

        return response;
    }

    //   Get Messages by Chatroom with Pagination
    @GetMapping
    @ApiOperation("Get Messages by Chatroom with Pagination")
    public Page<MessageResponse> getMessages(
            @RequestParam Long chatroomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return messageRepository.findByChatroom_Id(chatroomId, PageRequest.of(page, size))
                .map(this::toResponse);
    }

    // Delivered Receipt
    @PutMapping("/{messageId}/delivered")
    @ApiOperation("Delivered Receipt")
    public ResponseEntity<?> markAsDelivered(@PathVariable Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getIsDelivered()) {
            message.setIsDelivered(true);
            message.setDeliveredAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return ResponseEntity.ok("Message marked as delivered.");
    }

    // Read Receipt
    @PutMapping("/{messageId}/read")
    @ApiOperation("Read Receipt")
    public ResponseEntity<?> markAsRead(@PathVariable Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getIsRead()) {
            message.setIsRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }

        return ResponseEntity.ok("Message marked as read.");
    }

    // Editing Messages
    @PutMapping("/{messageId}/edit")
    @ApiOperation("Editing a sent Message")
    public ResponseEntity<?> editMessage(
            @PathVariable Long messageId,
            @RequestBody EditMessageRequest request
    ) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Only sender can edit
        if (!message.getSender().getId().equals(request.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only the sender can edit the message.");
        }

        if (request.getNewContent() == null || request.getNewContent().isBlank()) {
            return ResponseEntity.badRequest().body("New content cannot be empty.");
        }

        message.setContent(request.getNewContent());
        message.setEditedAt(LocalDateTime.now());

        messageRepository.save(message);
        return ResponseEntity.ok(toResponse(message));
    }

    // forward message to specific chatroom else throw error
    @PostMapping("/{messageId}/forward")
    @ApiOperation("forward message to specific chatroom else throw error")
    public MessageResponse forwardMessage(
            @PathVariable Long messageId,
            @RequestBody ForwardMessageRequest request
    ) {
        // Validate original message
        Message original = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Original message not found"));

        // Validate sender
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Validate target chatroom
        Chatroom targetChatroom = chatroomRepository.findById(request.getTargetChatroomId())
                .orElseThrow(() -> new RuntimeException("Target chatroom not found"));

        // Create a forwarded copy
        Message forwarded = Message.builder()
                .sender(sender)
                .chatroom(targetChatroom)
                .content(original.getContent())
                .attachmentPath(original.getAttachmentPath())
                .attachmentType(original.getAttachmentType())
                .isForwarded(true)
                .forwardedFromChatroom(original.getChatroom())
                .build();

        return toResponse(messageRepository.save(forwarded));
    }

    //   Convert Entity to DTO
    private MessageResponse toResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .chatroomId(message.getChatroom().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getDisplayName())
                .timestamp(message.getTimestamp())
                .attachmentPath(message.getAttachmentPath())
                .attachmentType(message.getAttachmentType())
                .isForwarded(message.getIsForwarded())
                .forwardedFromChatroomId(message.getForwardedFromChatroom() != null ?
                                message.getForwardedFromChatroom().getId() : null)
                .editedAt(message.getEditedAt())
                .isEdited(message.getEditedAt() != null)
                .build();
    }
}
