package com.whatsapp.repository;

import com.whatsapp.model.ChatroomParticipant;
import com.whatsapp.model.User;
import com.whatsapp.model.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatroomParticipantRepository extends JpaRepository<ChatroomParticipant, Long> {
    List<ChatroomParticipant> findByUser(User user);
    List<ChatroomParticipant> findByChatroom(Chatroom chatroom);
}