package com.whatsapp.repository;

import com.whatsapp.model.Chatroom;
import com.whatsapp.model.MuteSetting;
import com.whatsapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MuteSettingRepository extends JpaRepository<MuteSetting, Long> {

    //  Get mute setting for specific user and chatroom
    Optional<MuteSetting> findByUserAndChatroom(User user, Chatroom chatroom);

    //  For use with user/chatroom IDs directly
    Optional<MuteSetting> findByUser_IdAndChatroom_Id(Long userId, Long chatroomId);
}