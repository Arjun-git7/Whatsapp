package com.whatsapp.repository;

import com.whatsapp.model.Message;
import com.whatsapp.model.Chatroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByChatroom_Id(Long chatroomId, Pageable pageable);
}