package com.whatsapp.repository;

import com.whatsapp.model.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    List<Reaction> findByMessage_Id(Long messageId);
    List<Reaction> findByUser_Id(Long userId);
}
