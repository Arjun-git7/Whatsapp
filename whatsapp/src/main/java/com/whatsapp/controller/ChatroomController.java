package com.whatsapp.controller;

import com.whatsapp.dto.ChatroomResponse;
import com.whatsapp.dto.CreateChatroomRequest;
import com.whatsapp.dto.UpdateChatroomRequest;
import com.whatsapp.dto.UserSummary;
import com.whatsapp.model.Chatroom;
import com.whatsapp.model.ChatroomParticipant;
import com.whatsapp.model.MuteSetting;
import com.whatsapp.model.User;
import com.whatsapp.repository.ChatroomParticipantRepository;
import com.whatsapp.repository.ChatroomRepository;
import com.whatsapp.repository.UserRepository;
import com.whatsapp.repository.MuteSettingRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chatrooms")
@RequiredArgsConstructor
@Api(tags = "Chatroom APIs")
public class ChatroomController {

    @Autowired
    private final ChatroomRepository chatroomRepository;
    @Autowired
    private final ChatroomParticipantRepository participantRepository;
    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private MuteSettingRepository muteSettingRepository;

    // Create chatroom (group or one-to-one)
    @PostMapping
    @ApiOperation("Create a new chatroom")
    public ChatroomResponse createChatroom(@RequestBody CreateChatroomRequest request) {
        List<User> users = userRepository.findAllById(request.getParticipantIds());

        String chatroomName;
        if (request.getIsGroup()) {
            chatroomName = request.getName();
        } else {
            chatroomName = users.stream().map(User::getUsername).collect(Collectors.joining("_"));
        }

        Chatroom chatroom = Chatroom.builder()
                .name(chatroomName)
                .isGroup(request.getIsGroup())
                .build();

        Chatroom savedChatroom = chatroomRepository.save(chatroom);


        List<ChatroomParticipant> participants = users.stream()
                .map(user -> ChatroomParticipant.builder()
                        .chatroom(savedChatroom)
                        .user(user)
                        .build())
                .collect(Collectors.toList());

        participantRepository.saveAll(participants);

        return toResponse(savedChatroom, participants);
    }

    //  Get chatrooms for a user
    @GetMapping("/user/{userId}")
    @ApiOperation("Get chatroom by ID")
    public List<ChatroomResponse> getChatroomsForUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatroomParticipant> userRooms = participantRepository.findByUser(user);

        return userRooms.stream()
                .map(link -> {
                    Chatroom chatroom = link.getChatroom();
                    List<ChatroomParticipant> allParticipants = participantRepository.findByChatroom(chatroom);
                    return toResponse(chatroom, allParticipants);
                })
                .collect(Collectors.toList());
    }

    //  Update chatroom name (only group chats)
    @PutMapping("/{chatroomId}")
    @ApiOperation("Update chatroom name")
    public ResponseEntity<?> updateChatroomName(@PathVariable Long chatroomId,
                                                @RequestBody UpdateChatroomRequest request) {
        Chatroom chatroom = chatroomRepository.findById(chatroomId)
                .orElseThrow(() -> new RuntimeException("Chatroom not found"));

        if (!chatroom.getIsGroup()) {
            return ResponseEntity.badRequest().body("Cannot rename 1-to-1 chatroom");
        }

        chatroom.setName(request.getName());
        chatroomRepository.save(chatroom);
        return ResponseEntity.ok("Chatroom name updated");
    }

    //  Search chatroom by keyword (group name)
    @GetMapping("/search")
    @ApiOperation("Search chatrooms by keyword")
    public List<ChatroomResponse> searchChatroomByName(@RequestParam String keyword) {
        List<Chatroom> rooms = chatroomRepository.findByNameContainingIgnoreCase(keyword);

        return rooms.stream()
                .map(chatroom -> {
                    List<ChatroomParticipant> participants = participantRepository.findByChatroom(chatroom);
                    return toResponse(chatroom, participants);
                })
                .collect(Collectors.toList());
    }

    // Muting Chatroom
    @PostMapping("/{chatroomId}/mute")
    @ApiOperation("Muting chatroom")
    public ResponseEntity<?> muteChatroom(
            @PathVariable Long chatroomId,
            @RequestParam Long userId,
            @RequestParam(required = false) Long durationMinutes
    ) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Chatroom chatroom = chatroomRepository.findById(chatroomId).orElseThrow(() -> new RuntimeException("Chatroom not found"));

        MuteSetting setting = muteSettingRepository.findByUserAndChatroom(user, chatroom)
                .orElse(MuteSetting.builder().user(user).chatroom(chatroom).build());

        setting.setIsMuted(true);
        setting.setMutedUntil(durationMinutes != null
                ? LocalDateTime.now().plusMinutes(durationMinutes)
                : null);

        muteSettingRepository.save(setting);
        return ResponseEntity.ok("Chat muted successfully");
    }

    // Unmuting Chatroom
    @PutMapping("/{chatroomId}/unmute")
    @ApiOperation("Unmuting Chatroom")
    public ResponseEntity<?> unmuteChatroom(
            @PathVariable Long chatroomId,
            @RequestParam Long userId
    ) {
        MuteSetting setting = muteSettingRepository.findByUser_IdAndChatroom_Id(userId, chatroomId)
                .orElseThrow(() -> new RuntimeException("Mute setting not found"));

        setting.setIsMuted(false);
        setting.setMutedUntil(null);
        muteSettingRepository.save(setting);

        return ResponseEntity.ok("Chat unmuted");
    }

    //  Helper to convert to DTO
    private ChatroomResponse toResponse(Chatroom chatroom, List<ChatroomParticipant> participants) {
        List<UserSummary> participantSummaries = participants.stream()
                .map(p -> {
                    User u = p.getUser();
                    return UserSummary.builder()
                            .id(u.getId())
                            .username(u.getUsername())
                            .displayName(u.getDisplayName())
                            .build();
                })
                .collect(Collectors.toList());

        return ChatroomResponse.builder()
                .id(chatroom.getId())
                .name(chatroom.getName())
                .isGroup(chatroom.getIsGroup())
                .createdAt(chatroom.getCreatedAt())
                .participants(participantSummaries)
                .build();
    }
}
