package com.whatsapp.repository;

import com.whatsapp.model.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {

    //  This is the method you're missing
    List<Chatroom> findByNameContainingIgnoreCase(String keyword);
}
